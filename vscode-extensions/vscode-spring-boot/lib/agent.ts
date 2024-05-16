import { ActivatorOptions } from '@pivotal-tools/commons-vscode';
import * as vscode from 'vscode';
import { LanguageClient } from 'vscode-languageclient/node';
import { Uri, window } from 'vscode';
import cp from "child_process";
import { homedir } from 'os';
import { WorkspaceEdit } from "vscode-languageclient";
import path from 'path';
import fs from "fs";
import { getTargetGuideMardown, getWorkspaceRoot, getExecutable } from './utils/util';
import { createConverter } from "vscode-languageclient/lib/common/protocolConverter";
import { systemPrompt } from './utils/system-ai-prompt';
import { userPrompt } from './utils/user-ai-prompt';

interface Prompt {
    projName: string
    systemPrompt: string;
    userPrompt: string;
}

interface PromptResponse {
    description: string;
    shortPackageName: string;
    prompt: Prompt;
}

interface ExecutableBootProject {
    name: string;
    uri: string;
    mainClass: string;
    classpath: string[];
    gav: string;
    buildTool: string;
    springBootVersion: string;
}

const CONVERTER = createConverter(undefined, true, true);
const LANGUAGE_MODEL_ID = 'copilot-gpt-4';
const AGENT_ID = 'springboot';

interface SpringBootChatAgentResult extends vscode.ChatResult {
	metadata: {
        command: string;
    }
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

function replacePlaceholder(fileContent: string, match: ExecutableBootProject, question: string) {
    if(match !== null && match !== undefined) {
        const lastIndex = match.mainClass.lastIndexOf('.')
        const replacements = {
            'Spring Project Name': match.name,
            'Package Name': match.mainClass.substring(0, lastIndex),
            'Build Tool': match.buildTool,
            'Spring Boot Version': match.springBootVersion,
            'Description': question
        };
    
        for (const placeholder in replacements) {
            fileContent = fileContent.replace(new RegExp(placeholder, 'g'), replacements[placeholder]);
        }
    } else {
        fileContent = fileContent.replace(new RegExp('Description', 'g'), question);
    }
    return fileContent;
}

function enhancePrompt(question: string, cwd: string, projects: ExecutableBootProject[]): Thenable<Prompt> {

    const match = projects.find(project => project.uri.toString().replace('file:', '') === cwd);
    const prompt = {} as Prompt;

    prompt.systemPrompt = replacePlaceholder(systemPrompt, match, question);
    prompt.userPrompt = replacePlaceholder(userPrompt, match, question);
    prompt.projName = match?.name;
    return Promise.resolve(prompt);
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

export async function applyLspEdit(uri: Uri) {
    if (!uri) {
        uri = await getTargetGuideMardown();
    }
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

async function chatRequest(enhancedPrompt: Prompt, token: vscode.CancellationToken, question: string) {
    
    const messages = [
            new vscode.LanguageModelChatSystemMessage(enhancedPrompt.systemPrompt),
            new vscode.LanguageModelChatUserMessage(enhancedPrompt.userPrompt),
            new vscode.LanguageModelChatUserMessage(question)
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
            let chatResponse: vscode.LanguageModelChatResponse | undefined;
            try {
                chatResponse = await vscode.lm.sendChatRequest(LANGUAGE_MODEL_ID, messages, {}, token);
            } catch (error) {
                if (error instanceof vscode.LanguageModelError) {
                    console.log(error.message, error.code);
                }
                reject(error);
            }

            try {
                for await (const fragment of chatResponse.stream) {
                    response += fragment;
                }
                resolve(response);
            } catch (error) {
                if (error instanceof vscode.LanguageModelError) {
                    console.log(error.message, error.code)
                }
                reject(error);
            }
        });
    });
}

async function handleAiPrompts(request: vscode.ChatRequest, context: vscode.ChatContext, stream: vscode.ChatResponseStream, token: vscode.CancellationToken): Promise<SpringBootChatAgentResult> {

    const previousMessages = context.history.filter(h => {
        return h instanceof vscode.ChatRequestTurn && h.participant == AGENT_ID
    }) as vscode.ChatRequestTurn[];
    console.log(previousMessages);

    const previousMessagesResp = context.history.filter(h => {
        return h instanceof vscode.ChatResponseTurn && h.participant == AGENT_ID
    }) as vscode.ChatResponseTurn[];
    console.log(previousMessagesResp);

    const cwd = (await getWorkspaceRoot()).fsPath;

    // const vectorSearch = await vscode.commands.executeCommand("sts/copilot/search", {"question": request.prompt});
    // console.log(vectorSearch);

    const projects = await vscode.commands.executeCommand("sts/spring-boot/executableBootProjects") as ExecutableBootProject[];
    console.log(projects)
    // get enhanced prompt by getting the spring context from boot ls
    const enhancedPrompt = await enhancePrompt(request.prompt, cwd, projects);

    // chat request to copilot LLM
    const response = await chatRequest(enhancedPrompt, token, request.prompt);

    // write the response to markdown file
    await writeResponseToFile(response, enhancedPrompt.projName, cwd);

    const uri = await getTargetGuideMardown();
    // modify the response from copilot LLM i.e. make response Boot 3 compliant if necessary
    await enhanceResponse(uri, enhancedPrompt.projName, cwd);

    // return modified response to chat
    const documentContent = await vscode.workspace.fs.readFile(uri);
    const chatResponse = Buffer.from(documentContent).toString();
    stream.markdown(chatResponse);
    stream.button({
        command: 'vscode-spring-boot.agent.apply',
        title: vscode.l10n.t('Apply Changes!')
    });
    return { metadata: { command: 'prompt' } };
}

export function activate(
    _client: LanguageClient,
    _options: ActivatorOptions,
    context: vscode.ExtensionContext
) {

    const agent = vscode.chat.createChatParticipant(AGENT_ID, async (request, context, progress, token) => {
        return handleAiPrompts(request, context, progress, token);
    });
    agent.iconPath = vscode.Uri.joinPath(context.extensionUri, 'readme-imgs', 'spring-tools-icon.png');
}


