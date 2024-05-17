"use strict";
import { Cli, CliTaskProvider, SPRING_CLI_TASK_TYPE } from "./cli";
import { handleBootAdd, handleBootNew } from "./boot";
import { handleCatalogAdd, handleCatalogRemove } from "./project-catalog";
import { handleProjectAdd, handleProjectRemove } from "./project";
import { handleCommandAdd, handleCommandExecute, handleCommandNew, handleCommandRemove } from "./command";
import { ExtensionContext, commands, tasks } from "vscode";
import { handleAiAdd } from "./ai";
import { handleGuideApplyWorkspaceEdit } from "./guide";

export const CLI = new Cli();

export async function activate(context: ExtensionContext): Promise<void> {
    
    context.subscriptions.push(
        commands.registerCommand('vscode-spring-cli.boot.new', handleBootNew),
        commands.registerCommand('vscode-spring-cli.boot.add', handleBootAdd),
    
        commands.registerCommand('vscode-spring-cli.project-catalog.add', handleCatalogAdd),
        commands.registerCommand('vscode-spring-cli.project-catalog.remove', handleCatalogRemove),
    
        commands.registerCommand('vscode-spring-cli.project.add', handleProjectAdd),
        commands.registerCommand('vscode-spring-cli.project.remove', handleProjectRemove),
    
        commands.registerCommand('vscode-spring-cli.command.add', handleCommandAdd),
        commands.registerCommand('vscode-spring-cli.command.remove', handleCommandRemove),
        commands.registerCommand('vscode-spring-cli.command.new', handleCommandNew),
        commands.registerCommand('vscode-spring-cli.command.execute', handleCommandExecute),
    
        commands.registerCommand('vscode-spring-cli.ai.add', handleAiAdd),
    
        commands.registerCommand('vscode-spring-cli.guide.apply', handleGuideApplyWorkspaceEdit),
    
        tasks.registerTaskProvider(SPRING_CLI_TASK_TYPE, new CliTaskProvider(CLI))  
    );

}

export async function deactivate(): Promise<void> {
}

