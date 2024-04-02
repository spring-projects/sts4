import * as VSCode from 'vscode';
import { LanguageClient } from "vscode-languageclient/node";
import { ActivatorOptions } from '@pivotal-tools/commons-vscode';
import { commands, window } from 'vscode';

interface ProcessCommandInfo {
    processKey : string;
	label: string;
	action: string;
    projectName: string;
}

export interface RemoteBootApp {
    jmxurl: string;
    host: string;
    urlScheme: "https" | "http";
    port: string;
    manualConnect: boolean;
    keepChecking?: boolean;
    processId: string;
    processName: string;
    projectName?: string;
}

type BootAppState = "none" | "connecting" | "connected" | "disconnecting" | "disconnected";

let activeBootApp: RemoteBootApp | undefined;
let state: BootAppState

async function liveHoverConnectHandler() {
    //sts.vscode-spring-boot.codeAction
    const processData : ProcessCommandInfo[] = await VSCode.commands.executeCommand('sts/livedata/listProcesses');
    const choiceMap = new Map<string, ProcessCommandInfo>();
    const choices : string[] = [];
    processData.forEach(p => {
        let actionLabel = "";
        switch (p.action) {
            case "sts/livedata/connect":
                actionLabel = "Show"
                break;
            case "sts/livedata/refresh":
                actionLabel = "Refresh";
                break;
            case "sts/livedata/disconnect":
                actionLabel = "Hide";
                break;    
        }
        const choiceLabel = actionLabel + " Live Data from: " + p.label;
        choiceMap.set(choiceLabel, p);
        choices.push(choiceLabel);
    });
    if (choices) {
        const picked = await VSCode.window.showQuickPick(choices);
        if (picked) {
            const chosen = choiceMap.get(picked);
            if (activeBootApp?.jmxurl === chosen.processKey) {
                switch (chosen.action) {
                    case "sts/livedata/connect":
                        await commands.executeCommand('vscode-spring-boot.live.show.active');
                        break;
                    case "sts/livedata/disconnect":
                        await commands.executeCommand('vscode-spring-boot.live.hide.active');
                        break;
                    default:
                        await VSCode.commands.executeCommand(chosen.action, chosen);
                    }
            } else {
                await VSCode.commands.executeCommand(chosen.action, chosen);
            }
        }
    }
}

async function updateBootAppState(newState: BootAppState) {
    if (newState !== state) {
        state = newState;
        commands.executeCommand('setContext', 'vscode-spring-boot.active-app-state', state);
    }
}

/** Called when extension is activated */
export function activate(
        client: LanguageClient,
        options: ActivatorOptions,
        context: VSCode.ExtensionContext
) {
    context.subscriptions.push(

        commands.registerCommand('vscode-spring-boot.live-hover.connect', () => {
            if (client.isRunning()) {
                return liveHoverConnectHandler();
            } else {
                window.showErrorMessage("No Spring Boot project found. Action is only available for Spring Boot Projects");
            }
        }),

        commands.registerCommand("vscode-spring-boot.live.activate", async appData => {
            activeBootApp = appData;
            await commands.executeCommand('sts/livedata/localAdd', activeBootApp);
            updateBootAppState("disconnected");
        }),

        commands.registerCommand("vscode-spring-boot.live.deactivate", async () => {
            await commands.executeCommand('sts/livedata/localRemove', activeBootApp.jmxurl);
            activeBootApp = undefined;
            updateBootAppState("none");
        }),

        commands.registerCommand("vscode-spring-boot.live.show.active", async () => {
            try {
                updateBootAppState("connecting");
                await commands.executeCommand('sts/livedata/connect', {
                    processKey: activeBootApp.jmxurl
                });
                updateBootAppState("connected");
            } catch (error) {
                updateBootAppState("disconnected");
                throw error;
            }
        }),

        commands.registerCommand("vscode-spring-boot.live.refresh.active", async () => {
            await commands.executeCommand('sts/livedata/refresh', {
                processKey: activeBootApp.jmxurl
            });
        }),

        commands.registerCommand("vscode-spring-boot.live.hide.active", async () => {
            try {
                updateBootAppState("disconnecting");
                await commands.executeCommand('sts/livedata/disconnect', {
                    processKey: activeBootApp.jmxurl
                });
                updateBootAppState("disconnected");
            } catch (error) {
                updateBootAppState("connected");
                throw error;
            }
        }),

    );
}
