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
import { systemBoot2Prompt, systemBoot3Prompt, systemPrompt } from './utils/system-ai-prompt';
import { userPrompt } from './utils/user-ai-prompt';
import { SPRINGCLI } from './Main';

interface Prompt {
    projName: string
    systemPrompt: string;
    userPrompt: string;
}

interface BootProjectInfo {
    name: string;
    uri: string;
    mainClass: string;
    buildTool: string;
    springBootVersion: string;  
    javaVersion: string;
}

const CONVERTER = createConverter(undefined, true, true);
const MODEL_SELECTOR: vscode.LanguageModelChatSelector = { vendor: 'copilot', family: 'gpt-3.5-turbo' };
const AGENT_ID = 'springboot';

interface SpringBootChatAgentResult extends vscode.ChatResult {
	metadata: {
        command: string;
    }
}

function replacePlaceholder(fileContent: string, question: string, match?: BootProjectInfo, joinedVectorSearch?: string) {
    if(match !== null && match !== undefined) {
        const lastIndex = match.mainClass.lastIndexOf('.')
        const replacements = {
            'Spring Project Name': match.name,
            'Package Name': match.mainClass.substring(0, lastIndex),
            'Build Tool': match.buildTool,
            'Spring Boot Version': match.springBootVersion,
            'Description': question,
            'Java Version': match.javaVersion,
            'Contents': joinedVectorSearch
        };
    
        for (const placeholder in replacements) {
            fileContent = fileContent.replace(new RegExp(placeholder, 'g'), replacements[placeholder]);
        }
    } else {
        fileContent = fileContent.replace(new RegExp('Description', 'g'), question);
    }
    return fileContent;
}

async function enhancePrompt(question: string, cwd: string, projects: BootProjectInfo[]): Promise<Thenable<Prompt>> {
    let match;
    if(projects !== null || projects === undefined) {
        match = projects.find(project => project.uri.toString().replace('file:', '') === cwd);
    }
    const prompt = {} as Prompt;
    prompt.systemPrompt = replacePlaceholder(systemPrompt, question, match); 
    if(match !== null && match !== undefined && match.springBootVersion.startsWith('3')) {
        prompt.systemPrompt = prompt.systemPrompt + '\n' + systemBoot3Prompt;
    } else {
        prompt.systemPrompt = prompt.systemPrompt + '\n' + systemBoot2Prompt;
    }
    prompt.userPrompt = replacePlaceholder(userPrompt, question, match);
    prompt.projName = match?.name;
    return Promise.resolve(prompt);
}

export async function applyLspEdit(uri: Uri) {
    if (!uri) {
        uri = await getTargetGuideMardown();
    }
    const lspEdit = await SPRINGCLI.guideLspEdit(uri);
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
            vscode.LanguageModelChatMessage.User(enhancedPrompt.systemPrompt),
            vscode.LanguageModelChatMessage.User(enhancedPrompt.userPrompt),
            vscode.LanguageModelChatMessage.User(question)
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
                const [model] = await vscode.lm.selectChatModels(MODEL_SELECTOR);
                if (!model) {
                    reject("Chat Request failed: Language model not found");
                }
                const chatResponse = await model.sendRequest(messages, {}, token);

                for await (const fragment of chatResponse.text) {
                    response += fragment;
                }
                resolve(response);
            } catch (error) {
                if (error instanceof vscode.LanguageModelError) {
                    console.log(error.message, error.code, error.stack)
                } else {
                    throw error;
                }
                reject(error);
            }
        });
    });
}

async function handleAiPrompts(request: vscode.ChatRequest, context: vscode.ChatContext, stream: vscode.ChatResponseStream, token: vscode.CancellationToken): Promise<SpringBootChatAgentResult> {

    const cwd = (await getWorkspaceRoot()).fsPath;

    console.log(cwd)

    const projects = await vscode.commands.executeCommand("sts/spring-boot/bootProjectInfo", {projectUri: cwd}) as BootProjectInfo[];
    // const projects = await vscode.commands.executeCommand("sts/spring-boot/executableBootProjects") as ExecutableBootProject[];
    console.log(projects);

    // const execProj = await vscode.commands.executeCommand("sts/spring-boot/executableBootProjects") as BootProjectInfo[];
    // console.log(execProj);

    // get enhanced prompt by adding the spring context from boot ls
    const enhancedPrompt = await enhancePrompt(request.prompt, cwd, projects);
    console.log(enhancedPrompt.systemPrompt);
    console.log(enhancedPrompt.userPrompt);

    // chat request to copilot LLM
    const response = await chatRequest(enhancedPrompt, token, request.prompt);

    // write the response to markdown file
    await writeResponseToFile(response, enhancedPrompt.projName, cwd);

    const uri = await getTargetGuideMardown();
    let documentContent;

    if(uri !== null && uri !== undefined) {

        // modify the response from copilot LLM i.e. make response Boot 3 compliant if necessary
        const enhancedResponse = await SPRINGCLI.enhanceResponse(uri, enhancedPrompt.projName, cwd);
        writeResponseToFile(enhancedResponse, enhancedPrompt.projName, cwd);
        // return modified response to chat
        documentContent = await vscode.workspace.fs.readFile(uri);
    } else {
        documentContent = response;
    }
    
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

        if (request.command === 'add') {
            return handleAiPrompts(request, context, progress, token);
        }
        return handleAiPrompts(request, context, progress, token);
    });
    agent.iconPath = vscode.Uri.joinPath(context.extensionUri, 'readme-imgs', 'spring-tools-icon.png');
}