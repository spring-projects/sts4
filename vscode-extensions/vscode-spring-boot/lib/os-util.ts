'use strict';

import * as vscode from 'vscode';

// NOTE: be sure to add 'shelljs' to dependencies in package.json to enable building of PKS integration
// "dependencies": {
//     "@pivotal-tools/commons-vscode": "file:../commons-vscode/pivotal-tools-commons-vscode-0.2.2.tgz",
//     "shelljs": "^0.7.7",
//     "vscode-languageclient": "4.1.3"
//   },
import * as shelljs from 'shelljs';

export interface StandardResult {
    readonly stdout: string;
    readonly stderr: string;
    readonly code: number;
}

export type StdResultHandler = (std: StandardResult) => void;

export async function run(cmd: string): Promise<StandardResult> {
    try {
        return await new Promise<StandardResult>((resolve, reject) => {
            shelljs.exec(cmd, null, (code, stdout, stderr) => resolve({code: code, stdout : stdout, stderr : stderr}));
        });
    } catch (ex) {
        vscode.window.showErrorMessage(ex);
    }
}

async function isInstalled(name: string): Promise<StandardResult> {
    let command = `which ${name}`;
    return await run(command);
}

export function runInTerminal(terminalName: string, command: string): void {
    const options = {
        name: terminalName
    };
    const terminal = vscode.window.createTerminal(options);
    terminal.sendText(command);
    terminal.show();
}


export function runCommand(cmd: string, handler: StdResultHandler) {
    run(cmd).then(standardResult => {
        if (standardResult.code != 0 && standardResult.stderr) {
            vscode.window.showErrorMessage(standardResult.stderr);
        }
        else {
            handler(standardResult);
        }
    });
}

export function hasResults(code: number, stdout: string, stderr: string): boolean {
    return code === 0 && stdout != null;
}
