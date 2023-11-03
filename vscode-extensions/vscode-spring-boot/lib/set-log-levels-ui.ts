'use strict';

import * as VSCode from 'vscode';
import { LanguageClient } from "vscode-languageclient/node";
import { ActivatorOptions } from '@pivotal-tools/commons-vscode';
import { LiveProcess, LiveProcessLoggersUpdatedNotification, LiveProcessLogLevelUpdatedNotification, LiveProcessUpdatedLogLevel } from './notification';

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
        if (picked) {
            const chosen = choiceMap.get(picked);
            const loggers = await VSCode.commands.executeCommand('sts/livedata/getLoggers', chosen, {"endpoint": "loggers"});
        }
    }
}

async function getLoggersList(process: LiveProcess) {
    const loggers: LoggersData = await VSCode.commands.executeCommand('sts/livedata/fetch/loggersData', process);
    let items;
    if(loggers) {
        items = Object.keys(loggers.loggers).map(packageName => {
            const logger: Logger = loggers.loggers[packageName];
            const effectiveLevel = logger.effectiveLevel;
            const label = packageName + ' (' + effectiveLevel + ')';
            return {
                packageName,
                effectiveLevel,
                label
            };
        });
    }
    if(items) {
        const chosenPackage = await VSCode.window.showQuickPick(items);
        if (chosenPackage) {
            const chosenlogLevel = await VSCode.window.showQuickPick(loggers.levels);
            const changeLogLevel = await VSCode.commands.executeCommand('sts/livedata/configure/logLevel', {"endpoint": "loggers"}, process, chosenPackage, {"configuredLevel":chosenlogLevel});
        }
    }

}

async function logLevelUpdated(process: LiveProcessUpdatedLogLevel) {
    VSCode.window.showInformationMessage("The Log level for " + process.packageName + " has been updated from " + 
    process.effectiveLevel + " to " + process.configuredLevel);
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