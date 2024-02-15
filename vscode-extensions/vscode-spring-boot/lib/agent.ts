import { ActivatorOptions } from '@pivotal-tools/commons-vscode';
import * as vscode from 'vscode';
import { LanguageClient } from 'vscode-languageclient/node';
import { projectCreationPrompt } from './utils/create-spring-boot-project-prompt';
import { extractCodeBlocks } from './utils/response-handler'; // Import the necessary function from the appropriate module
import { Uri, window } from 'vscode';
import cp from "child_process";
import { homedir } from 'os';
import { WorkspaceEdit } from "vscode-languageclient";
import path from 'path';
import fs from "fs";
import { getTargetGuideMardown, getWorkspaceRoot, getExecutable } from './utils/util';
import { createConverter } from "vscode-languageclient/lib/common/protocolConverter";

interface Prompt {
    systemPrompt: string;
    userPrompt: string;
}

interface PromptResponse {
    description: string;
    shortPackageName: string;
    prompt: Prompt;
}

const CONVERTER = createConverter(undefined, true, true);

interface SpringBootChatAgentResult extends vscode.ChatAgentResult2 {
	slashCommand: string;
}

async function executeCommand(args: string[], cwd?: string): Promise<string> {
    const processOpts = { cwd: cwd || (await getWorkspaceRoot())?.fsPath || homedir() };
    const executable = getExecutable();
    const process = executable.endsWith(".jar") ? await cp.exec(`java -jar ${executable} ${args.join(" ")}`, processOpts) : await cp.exec(`${executable} ${args.join(" ")}`, processOpts);
    const dataChunks: string[] = [];
    process.stdout.on("data", s => dataChunks.push(s));
    return new Promise<string>((resolve, reject) => {
        process.on("exit", (code) => {
            if (code) {
                reject(`Failed to execute command: ${dataChunks.join()}`);
            } else {
                resolve(dataChunks.join());
            }
        });
    });
}

async function exec<T>(title: string, message: string, args: string[], cwd?: string): Promise<T> {
    return vscode.window.withProgress({
        location: vscode.ProgressLocation.Window,
        cancellable: true,
        title,
    }, async (progress, cancellation) => {

        if (message) {
            progress.report({message});
        }
        
        return new Promise<T>(async (resolve, reject) => {
            if (cancellation.isCancellationRequested) {
                reject("Cancelled");
            }
            try {
                const output: string = await executeCommand(args, cwd);
                console.log(output);
                resolve(output as T);
            } catch (error) {
                console.error(`Error: ${error}`);
                reject(error);
            }
        });
    });
}

async function fetchJson<T>(title: string, message: string, args: string[], cwd?: string): Promise<T> {
    return window.withProgress({
        location: vscode.ProgressLocation.Window,
        cancellable: true,
        title
    }, async (progress, cancellation) => {
        
        if (message) {
            progress.report({message});
        }
        return new Promise<T>(async (resolve, reject) => {
            if (cancellation.isCancellationRequested) {
                reject("Cancelled");
            }
            try {
                const output = await executeCommand(args, cwd);
                const extractJson = output.substring(output.indexOf('{'));
                resolve(JSON.parse(extractJson) as T);
            } catch (error) {
                reject(error);
            }
        });
    });
}

function springCliHandleAIPrompt(question: string, cwd: string): Thenable<PromptResponse> {

    const args = [
        "ai",
        "prompt",
        "--description",
        `"${question}"`
    ];
    return fetchJson("Spring cli ai prompt", question, args, cwd);
}

async function enhanceResponse(uri: Uri, projDescription: string, cwd: string) {
    const args = [
        "ai",
        "enhance-response",
        "--file",
        uri.fsPath
    ];
    const enhancedResponse: string = await exec("Spring cli ai", "Enhance response", args, cwd);
    writeResponseToFile(enhancedResponse, projDescription, cwd);
}


function fetchLspEdit(uri: Uri, cwd?: string): Promise<WorkspaceEdit> {
    const args = [
        "guide",
        "apply",
        "--lsp-edit",
        "--file",
        uri.fsPath
    ];
    return fetchJson("Spring cli ai", "Apply lsp edit", args, cwd || path.dirname(uri.fsPath));
}

async function applyLspEdit(uri: Uri) {
    const lspEdit = await fetchLspEdit(uri);
    const workspaceEdit = await CONVERTER.asWorkspaceEdit(lspEdit);

    await Promise.all(workspaceEdit.entries().map(async ([uri, edits]) => {
        if (fs.existsSync(uri.fsPath)) {
            const doc = await vscode.workspace.openTextDocument(uri.fsPath);
            await window.showTextDocument(doc);
        }
    }));
    
    return await vscode.workspace.applyEdit(workspaceEdit, {
        isRefactoring: true
    });
}

async function writeResponseToFile(response: string, shortPackageName: string, cwd: string) {
    const readmeFilePath =  path.resolve(cwd, `README-ai-${shortPackageName}.md`);
    if (fs.existsSync(readmeFilePath)) {
        try {
            fs.unlinkSync(readmeFilePath);
        } catch (ex) {
            throw new Error(`Could not delete readme file: ${readmeFilePath}, ${ex}`);
        }
    }
    
    try {
        fs.writeFileSync(readmeFilePath, response);
    } catch (ex) {
        throw new Error(`Could not write readme file: ${readmeFilePath}, ${ex}`);
    }
}

async function chatRequest(enhancedPrompt: PromptResponse, token: vscode.CancellationToken) {
    const access = await vscode.chat.requestChatAccess('copilot');

    const messages = [
        {
            role: vscode.ChatMessageRole.System,
            content: enhancedPrompt.prompt.systemPrompt
        },
        {
            role: vscode.ChatMessageRole.User,
            content: enhancedPrompt.prompt.userPrompt
        },
    ];
    let response = '';
    return vscode.window.withProgress({
        location: vscode.ProgressLocation.Window,
        title: "Copilot request",
        cancellable: true
    }, async (progress, cancellation) => {
        progress.report({ message: "processing" });
        return new Promise<string>(async (resolve, reject) => {
            if (cancellation.isCancellationRequested) {
                console.log("Chat request cancelled");
            }
            try {
                const chatRequest = access.makeRequest(messages, {}, token);
                for await (const fragment of chatRequest.response) {
                    response += fragment;
                }
                resolve(response);
            } catch (error) {
                reject(error);
            }
        });
    });
}

async function handleAiPrompts(request: vscode.ChatAgentRequest, context: vscode.ChatAgentContext, progress: vscode.Progress<vscode.ChatAgentProgress>, token: vscode.CancellationToken): Promise<SpringBootChatAgentResult> {

        if (request.slashCommand?.name == 'prompt') {
            const cwd = (await getWorkspaceRoot()).fsPath;
            // get enhanced prompt by calling spring cli `prompt` command
            const enhancedPrompt = await springCliHandleAIPrompt(request.prompt, cwd);

            // chat request to copilot LLM
            const response = await chatRequest(enhancedPrompt, token);

            // write the response to markdown file
            await writeResponseToFile(response, enhancedPrompt.shortPackageName, cwd);

            const uri = await getTargetGuideMardown();
            // modify the response from copilot LLM i.e. make response Boot 3 compliant if necessary
            await enhanceResponse(uri, enhancedPrompt.shortPackageName, cwd);

            // apply lsp edit by calling spring cli `guide apply` command
            applyLspEdit(uri);

            // return modified response to chat
            const documentContent = await vscode.workspace.fs.readFile(uri);
            const chatResponse = Buffer.from(documentContent).toString();
            progress.report({ content: chatResponse });
			return { slashCommand: 'prompt' };
        }  
}

async function handleCreateProject(request: vscode.ChatAgentRequest, context: vscode.ChatAgentContext, progress: vscode.Progress<vscode.ChatAgentProgress>, token: vscode.CancellationToken): Promise<SpringBootChatAgentResult> {
        if (request.slashCommand?.name == 'new') {
            const access = await vscode.chat.requestChatAccess('copilot');
            
            const systemPrompt = 'Your task is to create Java source code for a comprehensive Spring Boot application from scratch \n'+
            `# Instructions:
            \`\`\`
            ${projectCreationPrompt}\n
            \`\`\``;

            const messages = [
                {
                    role: vscode.ChatMessageRole.System,
                    content: systemPrompt
                },
                {
                    role: vscode.ChatMessageRole.User,
                    content: request.prompt
                }
            ];
            const chatRequest = access.makeRequest(messages, {}, token);
            let entireResponse = '';
            for await (const fragment of chatRequest.response) {
                entireResponse += fragment;            
            }
            const modifiedResponse = modifyResponse(entireResponse);
            console.log(modifiedResponse);
            progress.report({ content: entireResponse });
            return { slashCommand: 'new' };
        }
}

function modifyResponse(response) {
    const { javaCodeBlocks, xmlCodeBlocks } = extractCodeBlocks(response);
    return response;
}

async function handleUpdates(request: vscode.ChatAgentRequest, token: vscode.CancellationToken, progress: vscode.Progress<vscode.ChatAgentProgress>) {
    const access = await vscode.chat.requestChatAccess('copilot');

    const info = `
            Spring Boot 3 introduces a significant update requiring the transition from 'javax.' imports to 'jakarta.' imports. 
            Ensure that all imports using 'javax.' are now replaced with 'jakarta.' imports. 
            Focus on updating classes and interfaces under 'javax.' to 'jakarta.' and converting 'javax.persistence'
            imports to 'jakarta.persistence' for proper Jakarta EE compatibility. Additionally, set the Hibernate version
            to a minimum of Hibernate ORM 5.6.x, supporting Jakarta Persistence. The default configuration in Spring Boot 3
            should use Hibernate 6 and Flyway 9. Please generate code accordingly.`;

    const messages = [
        {
            role: vscode.ChatMessageRole.System,
            content: `You are a Spring Boot Language server part of Spring tools extension. Answer questions related to Spring boot projects in vscode.\n
                    Use the information below to help the user with the query. Make sure the correct imports are used.\n` + info
        },
        {
            role: vscode.ChatMessageRole.User,
            content: request.prompt
        },
    ];
    const chatRequest = access.makeRequest(messages, {}, token);
    for await (const fragment of chatRequest.response) {
        progress.report({ content: fragment });
    }
}


export function activate(
    client: LanguageClient,
    options: ActivatorOptions,
    context: vscode.ExtensionContext
) {

    const agent = vscode.chat.createChatAgent('springboot', async (request, context, progress, token) => {
		if (request.slashCommand?.name === 'prompt') {
			return handleAiPrompts(request, context, progress, token);
		} else if (request.slashCommand?.name === 'new') {
			return handleCreateProject(request, context, progress, token);
		} else {
            await handleUpdates(request, token, progress);
        }
    });

    agent.description = vscode.l10n.t('Hi! How can I help you with your spring boot project?');
	agent.fullName = vscode.l10n.t('Spring Boot');
    agent.slashCommandProvider = {
        provideSlashCommands(token) {
            return [
                { name: 'prompt', description: 'Handle AI Prompts through Spring cli' },
                { name: 'new', description: 'Create a new spring boot project'}
            ];
        }
    };
}


