"use strict";
import * as vscode from "vscode";
import { Cli } from "./cli";
import { handleBootAdd, handleBootNew } from "./boot";
import { handleCatalogAdd, handleCatalogRemove } from "./project-catalog";
import { handleProjectAdd, handleProjectRemove } from "./project";

export const CLI = new Cli();

export async function activate(context: vscode.ExtensionContext): Promise<void> {
    
    vscode.commands.registerCommand('vscode-spring-cli.boot.new', handleBootNew);
    vscode.commands.registerCommand('vscode-spring-cli.boot.add', handleBootAdd);

    vscode.commands.registerCommand('vscode-spring-cli.project-catalog.add', handleCatalogAdd);
    vscode.commands.registerCommand('vscode-spring-cli.project-catalog.remove', handleCatalogRemove);

    vscode.commands.registerCommand('vscode-spring-cli.project.add', handleProjectAdd);
    vscode.commands.registerCommand('vscode-spring-cli.project.remove', handleProjectRemove);
}

export async function deactivate(): Promise<void> {
}
