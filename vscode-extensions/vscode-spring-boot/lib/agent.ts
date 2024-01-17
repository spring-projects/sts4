import { ActivatorOptions } from '@pivotal-tools/commons-vscode';
import * as vscode from 'vscode';
import { LanguageClient } from 'vscode-languageclient/node';
// import * as fs from 'fs';
// import path from 'path';
import { releaseNotes } from './spring-boot-release-docs';

// const fileName = 'spring-boot-release-docs.md';
// const filePath = path.resolve(__dirname, fileName);

interface SpringBootChatAgentResult extends vscode.ChatAgentResult2 {
	slashCommand: string;
}

function agentHandle(): vscode.ChatAgentHandler {

    return async (request: vscode.ChatAgentRequest, context: vscode.ChatAgentContext, progress: vscode.Progress<vscode.ChatAgentProgress>, token: vscode.CancellationToken): Promise<SpringBootChatAgentResult> => {
        if (request.slashCommand?.name == 'rewrite') {
            const access = await vscode.chat.requestChatAccess('copilot');
            const messages = [
                {
                    role: vscode.ChatMessageRole.System,
					content: 'You are a Spring Boot Language server part of Spring tools extension. Answer questions about Spring boot projects in vscode.'
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
			return { slashCommand: 'rewrite' };
        } else if (request.slashCommand?.name == 'test') {
            const access = await vscode.chat.requestChatAccess('copilot');
            const detailedPrompt = 'This is a Spring boot project running on java 17 and spring boot version 3.0.1'  + request.prompt + '\n\n';
            const messages = [
                {
                    role: vscode.ChatMessageRole.System,
					content: 'You are a Spring Boot Language server part of Spring tools extension. Answer questions about Spring boot projects in vscode.'
                },
                {
                    role: vscode.ChatMessageRole.User,
                    content: detailedPrompt
                }
            ];
            const chatRequest = access.makeRequest(messages, {}, token);
            for await (const fragment of chatRequest.response) {
                progress.report({ content: fragment });
            }
			return { slashCommand: 'test' };
        } else if (request.slashCommand?.name == 'release') {
            const access = await vscode.chat.requestChatAccess('copilot');
            let fileContent: string;
            console.log(__dirname);
            console.log('Current working directory:', process.cwd());
            // try {
            //     const data = fs.readFileSync('spring-boot-release-docs.md', 'utf8');
            //     console.log(data);
            //     fileContent = data;
            // } catch (err) {
            //     console.error(`Error reading file from disk: ${err}`);
            // }
            fileContent = releaseNotes;
            
            const detailedPrompt = 'This is the release notes for spring boot 3.'+ fileContent +'This is a Spring boot project running on java 17 and spring boot version 3.0.1'  + request.prompt + '\n\n';
            const messages = [
                {
                    role: vscode.ChatMessageRole.System,
					content: 'You are a Spring Boot Language server part of Spring tools extension. Answer questions about Spring boot projects in vscode.'
                },
                {
                    role: vscode.ChatMessageRole.User,
                    content: detailedPrompt
                }
            ];
            const chatRequest = access.makeRequest(messages, {}, token);
            for await (const fragment of chatRequest.response) {
                progress.report({ content: fragment });
            }
			return { slashCommand: 'release' };
        }
    }
}

export function activate(
    client: LanguageClient,
    options: ActivatorOptions,
    context: vscode.ExtensionContext
) {

    const agent = vscode.chat.createChatAgent('springboot', agentHandle());
    // agent.iconPath = vscode.Uri.joinPath(context.extensionUri, 'cat.jpeg');
    agent.description = vscode.l10n.t('Hi! What can I help you with?');
	agent.fullName = vscode.l10n.t('Spring Boot');
    agent.slashCommandProvider = {
        provideSlashCommands(token) {
            return [
                { name: 'rewrite', description: 'Handle rewrite recipes for spring boot' },
                { name: 'test', description: 'Handle queries for spring boot projects' },
                { name: 'release', description: 'Handle Spring boot 3 release queries' },
            ];
        }
    };

}
