"use strict";
import * as vscode from "vscode";
import { Cli } from "./cli";
import { handleBootAdd, handleBootNew } from "./boot";
import { handleCatalogAdd, handleCatalogRemove } from "./project-catalog";

export const CLI = new Cli();

export async function activate(context: vscode.ExtensionContext): Promise<void> {
    vscode.commands.registerCommand('vscode-spring-cli.boot.new', handleBootNew);
    vscode.commands.registerCommand('vscode-spring-cli.boot.add', handleBootAdd);
    vscode.commands.registerCommand('vscode-spring-cli.project-catalog.add', handleCatalogAdd);
    vscode.commands.registerCommand('vscode-spring-cli.project-catalog.remove', handleCatalogRemove);
}

export async function deactivate(): Promise<void> {
}
