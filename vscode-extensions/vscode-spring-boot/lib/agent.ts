import { ActivatorOptions } from '@pivotal-tools/commons-vscode';
import * as vscode from 'vscode';
import { LanguageClient } from 'vscode-languageclient/node';
import { releaseNotes } from './utils/spring-boot-release-docs';
import { projectCreationPrompt } from './utils/create-spring-boot-project-prompt';
import { extractCodeBlocks } from './utils/response-handler'; // Import the necessary function from the appropriate module


interface SpringBootChatAgentResult extends vscode.ChatAgentResult2 {
	slashCommand: string;
}

async function handleValidations(request: vscode.ChatAgentRequest, context: vscode.ChatAgentContext, progress: vscode.Progress<vscode.ChatAgentProgress>, token: vscode.CancellationToken): Promise<SpringBootChatAgentResult> {

        if (request.slashCommand?.name == 'validations') {
            const access = await vscode.chat.requestChatAccess('copilot');
            const extension = vscode.extensions.getExtension('vmware.vscode-spring-boot');
            let detailedPrompt = '';

            if (extension) {
                const packageJson = extension.packageJSON;

                // Check if the extension has configuration settings
                if (packageJson.contributes && packageJson.contributes.configuration) {
                    const configurations = packageJson.contributes.configuration;
                    let boot2Validations: any[]=[];
                    let boot3Validations: any[]=[];
                    configurations.forEach((config: any) => {
                        if(config.id === 'boot2') {
                            boot2Validations.push(config.properties);
                        }

                        if(config.id === 'boot3') {
                            boot3Validations.push(config.properties);
                        }
                    });
                    detailedPrompt += 'This is the list of configurations and settings available in spring tools extensions to enable validations and best practices for spring boot applications. They help to improve code quality of your project by adopting to the changes that come up in every release.' 
                    + `\n` + 'Boot 2.x Best Practices & Optimizations' +JSON.stringify(boot2Validations)+ `\n`
                    + `\n` + 'Boot 3.x Best Practices & Optimizations' +JSON.stringify(boot3Validations)+ `\n`;
                    console.log(detailedPrompt);
                } else {
                    console.log(`Extension 'vscode-spring-boot' does not contribute configuration settings.`);
                }
            } else {
                console.log(`Extension with identifier 'vscode-spring-boot' not found.`);
            }
        
            detailedPrompt += request.prompt;
        
            // const extensionConfig = vscode.workspace.getConfiguration('vmware.vscode-spring-boot');
            console.log(detailedPrompt);
            const messages = [
                {
                    role: vscode.ChatMessageRole.System,
					content: 'You are a Spring Boot Language server part of Spring tools extension. Answer questions about validation and best practices for Spring boot projects in vscode. Help the user with configuration or settings to enable best practice validation in spring boot apps.'
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
			return { slashCommand: 'validations' };
        }  
}

async function handleRewrite(request: vscode.ChatAgentRequest, context: vscode.ChatAgentContext, progress: vscode.Progress<vscode.ChatAgentProgress>, token: vscode.CancellationToken): Promise<SpringBootChatAgentResult> {
    if (request.slashCommand?.name == 'rewrite') {
        const access = await vscode.chat.requestChatAccess('copilot');
        const extension = vscode.extensions.getExtension('vmware.vscode-spring-boot');
        let detailedPrompt = '';
        if (extension) {
            const packageJson = extension.packageJSON;

            // Check if the extension has configuration settings
            if (packageJson.contributes && packageJson.contributes.configuration) {
                const configurations = packageJson.contributes.configuration;
                let configIds: any[] =[];
                let rewriteProp: any[]=[];
                console.log(configurations[0]);
                configurations.forEach((config: any) => {
                    console.log(config);
                    configIds.push(config.id);
                    if(config.id === 'rewrite') {
                        rewriteProp.push(config.properties);
                    }
                });
                console.log(configIds);
                console.log(rewriteProp);
                detailedPrompt += 'List of rewrite settings available in vscode for spring boot apps' + `\n` + JSON.stringify(rewriteProp)+ `\n`;
                console.log(detailedPrompt);
            } else {
                console.log(`Extension 'vscode-spring-boot' does not contribute configuration settings.`);
            }
        } else {
            console.log(`Extension with identifier 'vscode-spring-boot' not found.`);
        }
        detailedPrompt += 'This is a Spring boot project running on java 17 and spring boot version 3.0.1'  + request.prompt + '\n\n';
        const messages = [
            {
                role: vscode.ChatMessageRole.System,
                content: 'You are a Spring Boot Language server part of Spring tools extension. Answer questions about Spring boot projects in vscode. Help the user with vscode settings available in vscode'
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
        return { slashCommand:'rewrite' };
    } 
}

async function handleRelease(request: vscode.ChatAgentRequest, context: vscode.ChatAgentContext, progress: vscode.Progress<vscode.ChatAgentProgress>, token: vscode.CancellationToken): Promise<SpringBootChatAgentResult> {
    if (request.slashCommand?.name == 'release') {
        const access = await vscode.chat.requestChatAccess('copilot');
        let fileContent: string;
        fileContent = releaseNotes;
        const editor = vscode.window.activeTextEditor;
        if (!editor) {
            return { slashCommand: 'release' };
        }

        const selection = editor.selection;
        if (selection.isEmpty) {
            return { slashCommand: 'release' };
        }

        const document = editor.document;
        const text = document.getText(selection);
        
        const detailedPrompt = 'This is the release notes for spring boot 3. \n'+ fileContent +'\n This is a Spring boot project running on java 17 and spring boot version 3.0.1'  
        + 
        `# Code:
        \`\`\`
        ${text}
        \`\`\``
        + request.prompt + '\n\n';
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

export function activate(
    client: LanguageClient,
    options: ActivatorOptions,
    context: vscode.ExtensionContext
) {

    const agent = vscode.chat.createChatAgent('springboot', async (request, context, progress, token) => {
		if (request.slashCommand?.name === 'release') {
			return handleRelease(request, context, progress, token);
		} else if (request.slashCommand?.name === 'rewrite') {
			return handleRewrite(request, context, progress, token);
		} else if (request.slashCommand?.name === 'validations') {
			return handleValidations(request, context, progress, token);
		} else if (request.slashCommand?.name === 'new') {
			return handleCreateProject(request, context, progress, token);
		} else {
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
                    Use the information below to help the user with the query. Make sure the correct imports are used.\n`+info
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
    });
    agent.description = vscode.l10n.t('Hi! How can I help you with your spring boot project?');
	agent.fullName = vscode.l10n.t('Spring Boot');
    agent.slashCommandProvider = {
        provideSlashCommands(token) {
            return [
                { name: 'rewrite', description: 'Handle rewrite recipes for spring boot projects' },
                { name: 'validations', description: 'Handle queries for spring boot validations' },
                { name: 'release', description: 'Handle Spring boot 3 queries' },
                { name: 'new', description: 'Create a new spring boot project'}
            ];
        }
    };

}
