import { ActivatorOptions } from '@pivotal-tools/commons-vscode';
import * as vscode from 'vscode';
import { LanguageClient } from 'vscode-languageclient/node';
import { projectCreationPrompt } from './utils/create-spring-boot-project-prompt';
import { extractCodeBlocks } from './utils/response-handler'; // Import the necessary function from the appropriate module
import { Uri, window } from 'vscode';
import cp, { exec } from "child_process";
import { homedir } from 'os';
import path from 'path';
import { getTargetGuideMardown, getWorkspaceRoot } from './utils/util';


interface SpringBootChatAgentResult extends vscode.ChatAgentResult2 {
	slashCommand: string;
}

function executable(): string {
    return vscode.workspace.getConfiguration("spring-cli").get("executable") || "spring";
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
            const processOpts = {cwd: cwd || homedir()};
            const execCmd = executable();
            console.log(execCmd);
            const process = execCmd.endsWith(".jar") ? await cp.exec(`java -jar ${execCmd} ${args.join(" ")}`, processOpts) : await cp.exec(`${execCmd} ${args.join(" ")}`, processOpts);
            cancellation.onCancellationRequested(() => process.kill());
            const errorMessageChunks = [];
            let guideFileName;
            process.stdout.on("data", s => {
                const res = /README-\S+.md/.exec(s);
                if (res.length) {
                    guideFileName = res[0].trim();
                }
            });
            process.stderr.on("data", s => errorMessageChunks.push(s))
            process.on("exit", (code) => {
                if (code) {
                    reject(`Failed to get response from LLM. ${errorMessageChunks.join()}`);
                } else {
                    resolve(Uri.file(path.join(cwd, guideFileName)));
                }
            });
        });
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
            // applyLspEdit();
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


