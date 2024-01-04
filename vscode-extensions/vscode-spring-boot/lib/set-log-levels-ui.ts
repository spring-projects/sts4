'use strict';

import * as VSCode from 'vscode';
import { LanguageClient } from "vscode-languageclient/node";
import { ActivatorOptions } from '@pivotal-tools/commons-vscode';
import { LiveProcess, LiveProcessLogLevelUpdatedNotification, LiveProcessUpdatedLogLevel } from './notification';

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

export interface ProcessLoggersData {
    loggers: LoggersData;
    processID: string;
    processName: string;
    processType: number;
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
            if (!choiceMap.has(choiceLabel)) {
                choiceMap.set(choiceLabel, p);
                choices.push(choiceLabel);
            }
        }
    });
    if (choices) {
        const picked = await VSCode.window.showQuickPick(choices);
        if (picked) {
            const chosen = choiceMap.get(picked);
            try {
                const loggers: ProcessLoggersData = await getLoggers(chosen);
                await displayLoggers(loggers, chosen.processKey);
              } catch (error) {
                VSCode.window.showErrorMessage("Failed to fetch loggers for the process " + chosen.processKey);
              }
        }
    }
}

async function getLoggers(processInfo: ProcessCommandInfo): Promise<ProcessLoggersData> {

    return new Promise(async (resolve, reject) => {
        await VSCode.window.withProgress({
          location: VSCode.ProgressLocation.Window,
          title: "Fetching Loggers Data for process "+processInfo.processKey,
          cancellable: false
        }, async (progress) => {
          try {
            const loggers: ProcessLoggersData = await VSCode.commands.executeCommand('sts/livedata/getLoggers', processInfo, {"endpoint": "loggers"}); 
            progress.report({});
            resolve(loggers);
          } catch (error) {
            reject(error);
          }
        });
      });
}

async function displayLoggers(processLoggersData: ProcessLoggersData, processKey: string) {
    let items;
    const loggersData = processLoggersData.loggers;
    if(loggersData.loggers) {
        items = Object.keys(loggersData.loggers).map(packageName => {
            const logger: Logger = loggersData.loggers[packageName];
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
            const chosenlogLevel = await VSCode.window.showQuickPick(loggersData.levels);
            await VSCode.commands.executeCommand('sts/livedata/configure/logLevel', {"processKey": processKey}, chosenPackage, {"configuredLevel":chosenlogLevel});
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
    client.onNotification(LiveProcessLogLevelUpdatedNotification.type, logLevelUpdated)
    context.subscriptions.push(
        VSCode.commands.registerCommand('vscode-spring-boot.set.log-levels', () => {
            if (client.isRunning()) {
                return setLogLevelHandler();
            } else {
                VSCode.window.showErrorMessage("No Spring Boot project found. Action is only available for Spring Boot Projects");
            }
        })
    );
}