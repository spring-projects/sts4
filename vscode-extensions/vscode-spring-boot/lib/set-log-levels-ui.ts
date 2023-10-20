'use strict';

import * as VSCode from 'vscode';
import { LanguageClient } from "vscode-languageclient/node";
import { ActivatorOptions } from '@pivotal-tools/commons-vscode';
import { LiveProcess, LiveProcessLoggersUpdatedNotification, LiveProcessLogLevelUpdatedNotification } from './notification';

interface ProcessCommandInfo {
    processKey : string;
	label: string;
	action: string;
    projectName: string;
}

export interface Logger {
    configuredLevel: string;
    effectiveLevel: string;
}

export interface Loggers {
    [propName: string]: Logger;
}

export interface LoggersData {
    levels: string[];
    loggers: Loggers;
}

export interface LoggerItem {
    logger: Logger;
    name: string;
}

async function setLogLevelHandler() {

    const processData : ProcessCommandInfo[] = await VSCode.commands.executeCommand('sts/livedata/listProcesses');
    console.log(processData)
    const choiceMap = new Map<string, ProcessCommandInfo>();
    const choices : string[] = [];
    processData.forEach(p => {
        const slash = p.action.lastIndexOf('/');
        if (slash>=0) {
            const choiceLabel = p.label;
            choiceMap.set(choiceLabel, p);
            choices.push(choiceLabel);
        }
    });
    if (choices) {
        const picked = await VSCode.window.showQuickPick(choices);
        console.log(picked)
        if (picked) {
            const chosen = choiceMap.get(picked);
            console.log(chosen)
            const loggers = await VSCode.commands.executeCommand('sts/livedata/getLoggers', chosen, {"endpoint": "loggers"});
            console.log(loggers)
        }
    }
}

async function getLoggersList(process: LiveProcess) {
    console.log("On loggers notification")
    const loggers: LoggersData = await VSCode.commands.executeCommand('sts/livedata/fetch/loggersData', process);
    console.log(loggers);
    let items;
    if(loggers) {
        items = Object.keys(loggers.loggers).map(packageName => {
            const logger: Logger = loggers.loggers[packageName];
            const label = packageName + ' (' + logger.effectiveLevel + ')';
            return {
                packageName,
                logger,
                label
            };
        });
    }
    console.log(loggers.levels)
    if(items) {
        const chosenPackage = await VSCode.window.showQuickPick(items);
        console.log(chosenPackage)
        if (chosenPackage) {
            const chosenlogLevel = await VSCode.window.showQuickPick(loggers.levels);
            console.log(chosenlogLevel)
            const changeLogLevel = await VSCode.commands.executeCommand('sts/livedata/change/logLevel', chosenPackage, {"configuredLevel":chosenlogLevel});
            console.log(changeLogLevel)
        }
    }

}

async function logLevelUpdated(process: LiveProcess) {
    console.log("On log level updated notifications")
}

/** Called when extension is activated */
export function activate(
        client: LanguageClient,
        options: ActivatorOptions,
        context: VSCode.ExtensionContext
) {
    context.subscriptions.push(
        VSCode.commands.registerCommand('vscode-spring-boot.set.log-levels', () => {
            if (client.isRunning()) {
                client.onNotification(LiveProcessLoggersUpdatedNotification.type, getLoggersList)
                client.onNotification(LiveProcessLogLevelUpdatedNotification.type, logLevelUpdated)
                return setLogLevelHandler();
            } else {
                VSCode.window.showErrorMessage("No Spring Boot project found. Action is only available for Spring Boot Projects");
            }
        })
    );
}