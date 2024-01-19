"use strict";
import { Cli, CliTaskProvider, SPRING_CLI_TASK_TYPE } from "./cli";
import { handleBootAdd, handleBootNew } from "./boot";
import { handleCatalogAdd, handleCatalogRemove } from "./project-catalog";
import { handleProjectAdd, handleProjectRemove } from "./project";
import { handleCommandAdd, handleCommandNew, handleCommandRemove } from "./command";
import { ExtensionContext, commands, tasks } from "vscode";

export const CLI = new Cli();

export async function activate(context: ExtensionContext): Promise<void> {
    
    context.subscriptions.push(...[
        
    ]);
    context.subscriptions.push(commands.registerCommand('vscode-spring-cli.boot.new', handleBootNew));
    context.subscriptions.push(commands.registerCommand('vscode-spring-cli.boot.add', handleBootAdd));

    context.subscriptions.push(commands.registerCommand('vscode-spring-cli.project-catalog.add', handleCatalogAdd));
    context.subscriptions.push(commands.registerCommand('vscode-spring-cli.project-catalog.remove', handleCatalogRemove));

    context.subscriptions.push(commands.registerCommand('vscode-spring-cli.project.add', handleProjectAdd));
    context.subscriptions.push(commands.registerCommand('vscode-spring-cli.project.remove', handleProjectRemove));

    context.subscriptions.push(commands.registerCommand('vscode-spring-cli.command.add', handleCommandAdd));
    context.subscriptions.push(commands.registerCommand('vscode-spring-cli.command.remove', handleCommandRemove));
    context.subscriptions.push(commands.registerCommand('vscode-spring-cli.command.new', handleCommandNew));

    context.subscriptions.push(tasks.registerTaskProvider(SPRING_CLI_TASK_TYPE, new CliTaskProvider(CLI)));

}

export async function deactivate(): Promise<void> {
}
