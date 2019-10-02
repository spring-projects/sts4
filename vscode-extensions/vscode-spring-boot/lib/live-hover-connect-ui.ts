'use strict';

import * as VSCode from 'vscode';
import { LanguageClient } from "vscode-languageclient";
import { ActivatorOptions } from '@pivotal-tools/commons-vscode';
import { stringify } from 'querystring';

interface ProcessCommandInfo {
    processKey : string;
	label: string;
	action: string
}

async function liveHoverConnectHandler() {

    //sts.vscode-spring-boot.codeAction
    const processData : ProcessCommandInfo[] = await VSCode.commands.executeCommand('sts/livedata/listProcesses');
    const choiceMap = new Map<string, ProcessCommandInfo>();
    const choices : string[] = [];
    processData.forEach(p => {
        const slash = p.action.lastIndexOf('/');
        if (slash>=0) {
            var actionLabel = p.action.substring(slash+1);
            actionLabel = actionLabel.substring(0, 1).toUpperCase() + actionLabel.substring(1);
            const choiceLabel = actionLabel + " " + p.label;
            choiceMap.set(choiceLabel, p);
            choices.push(choiceLabel);
        }
    });
    if (choices) {
        const picked = await VSCode.window.showQuickPick(choices);
        if (picked) {
            const chosen = choiceMap.get(picked);
            await VSCode.commands.executeCommand(chosen.action, chosen);
        }
    }
}

/** Called when extension is activated */
export function activate(
        client: LanguageClient,
        options: ActivatorOptions, 
        context: VSCode.ExtensionContext
) {
    context.subscriptions.push(
        VSCode.commands.registerCommand('vscode-spring-boot.live-hover.connect', liveHoverConnectHandler)
    );
}
