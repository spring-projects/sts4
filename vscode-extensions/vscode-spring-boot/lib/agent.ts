import { ActivatorOptions } from '@pivotal-tools/commons-vscode';
import * as vscode from 'vscode';
import { LanguageClient } from 'vscode-languageclient/node';
import { projectCreationPrompt } from './utils/create-spring-boot-project-prompt';
import { extractCodeBlocks } from './utils/response-handler'; // Import the necessary function from the appropriate module
import { Uri, window } from 'vscode';
import cp, { exec } from "child_process";
import { homedir } from 'os';
import { WorkspaceEdit } from "vscode-languageclient";
import path from 'path';
import fs from "fs";
import { getTargetGuideMardown, getWorkspaceRoot } from './utils/util';
import { createConverter } from "vscode-languageclient/lib/common/protocolConverter";

const CONVERTER = createConverter(undefined, true, true);
interface SpringBootChatAgentResult extends vscode.ChatAgentResult2 {
	slashCommand: string;
}

function executable(): string {
    return vscode.workspace.getConfiguration("spring-cli").get("executable") || "spring";
}

async function executeCommand(args: string[], cwd?: string, jsonOutput: boolean = false): Promise<string> {
    const processOpts = { cwd: cwd || getWorkspaceRoot()?.fsPath || homedir() };
    const execCmd = executable();
    const process = execCmd.endsWith(".jar") ? await cp.exec(`java -jar ${execCmd} ${args.join(" ")}`, processOpts) : await cp.exec(`${execCmd} ${args.join(" ")}`, processOpts);
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

function springCli(question: string, cwd: string): Thenable<Uri> {

    const args = [
        "ai",
        "add",
        "--preview",
        "true",
        "--description",
        `"${question}"`
    ];

    return vscode.window.withProgress({
        location: vscode.ProgressLocation.Window,
        cancellable: true,
        title: "Spring CLI call",
    }, (progress, cancellation) => {

        progress.report({ message: question });
        
        return new Promise<Uri>(async (resolve, reject) => {
            if (cancellation.isCancellationRequested) {
                reject("Cancelled");
            }
            try {
                const output = await executeCommand(args, cwd);
                const res = /README-\S+.md/.exec(output);
                if (res.length) {
                    resolve(Uri.file(path.join(cwd, res[0].trim())));
                } else {
                    reject("Failed to get response from LLM.");
                }
            } catch (error) {
                console.error(`Error: ${error}`); // Log error
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
                const output = await executeCommand(args, cwd, true);
                console.log(output);
                resolve(JSON.parse(output) as T);
            } catch (error) {
                reject(error);
            }
        });
    });
}


function fetchLspEdit(uri: Uri, cwd?: string): Promise<WorkspaceEdit> {
    const args = [
        "guide",
        "apply",
        "--lsp-edit",
        "--file",
        uri.fsPath
    ];
    return fetchJson("Lsp Edit", uri.fsPath, args, cwd || path.dirname(uri.fsPath));
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

async function handleAiPrompts(request: vscode.ChatAgentRequest, context: vscode.ChatAgentContext, progress: vscode.Progress<vscode.ChatAgentProgress>, token: vscode.CancellationToken): Promise<SpringBootChatAgentResult> {

        if (request.slashCommand?.name == 'ai-prompts') {

            await springCli(request.prompt, getWorkspaceRoot().fsPath);
            const uri = await getTargetGuideMardown();
            const documentContent = await vscode.workspace.fs.readFile(uri);
            const contentString = Buffer.from(documentContent).toString();
            
            const access = await vscode.chat.requestChatAccess('copilot');
            let detailedPrompt = '';

            detailedPrompt += request.prompt;

            const systemPrompt = 'Your task is to create Java source code for a comprehensive Spring Boot application from scratch using below instructions \n'+
            `# Instructions:
            \`\`\`
            ${contentString}\n
            \`\`\``;
        
            console.log(detailedPrompt);
            console.log(systemPrompt);
            const messages = [
                {
                    role: vscode.ChatMessageRole.System,
					content: systemPrompt
                },
                {
                    role: vscode.ChatMessageRole.User,
                    content: request.prompt
                },
            ];
            const chatRequest = access.makeRequest(messages, {}, token);
            let response = '';
            for await (const fragment of chatRequest.response) {
                response += fragment;
            }
            applyLspEdit(uri);
           progress.report({ content: response });
			return { slashCommand: 'ai-prompt' };
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
		if (request.slashCommand?.name === 'ai-prompts') {
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
                { name: 'ai-prompts', description: 'Handle AI Prompts through Spring CLI' },
                { name: 'new', description: 'Create a new spring boot project'}
            ];
        }
    };
}


