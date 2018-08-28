'use strict';

import * as VSCode from 'vscode';
import { Position } from 'vscode-languageclient';



export function registerCommands(context: VSCode.ExtensionContext) {
    VSCode.commands.getCommands(false).then(commands => { 
        if (!commandExists(commands, "sts.open.url")) {
            registerOpenUrl(context, "sts.open.url");
        }
    
        if (!commandExists(commands, "sts.showHoverAtPosition")) {
            registerShowHoverAtPosition(context, "sts.showHoverAtPosition");
        }
    });
}

function registerOpenUrl(context: VSCode.ExtensionContext, commandId: string) {
    context.subscriptions.push(VSCode.commands.registerCommand(commandId, (url) => {
        VSCode.commands.executeCommand('vscode.open', VSCode.Uri.parse(url))
    }));
}

function registerShowHoverAtPosition(context: VSCode.ExtensionContext, commandId: string) {
    VSCode.commands.registerCommand(commandId, (position: Position) => {
        const editor = VSCode.window.activeTextEditor;
        const vsPosition = new VSCode.Position(position.line, position.character);
        editor.selection = new VSCode.Selection(vsPosition, vsPosition);
        VSCode.commands.executeCommand('editor.action.showHover');
    });
}

function commandExists(commands: string[], commandId: string) {
    return commands.indexOf(commandId) >= 0;
}