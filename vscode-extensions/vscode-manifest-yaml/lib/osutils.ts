'use strict';

import * as vscode from 'vscode';

// NOTE: be sure to add 'shelljs' to dependencies in package.json to enable building of PKS integration
// "dependencies": {
//     "@pivotal-tools/commons-vscode": "file:../commons-vscode/pivotal-tools-commons-vscode-0.2.2.tgz",
//     "shelljs": "^0.7.7",
//     "vscode-languageclient": "4.1.3"
//   },
import * as shelljs from 'shelljs';

export interface Std {
    readonly stdout: string;
    readonly stderr: string;
    readonly code: number;
}

export async function run(cmd: string): Promise<Std> {
    try {
        return await new Promise<Std>((resolve, reject) => {
            shelljs.exec(cmd, null, (code, stdout, stderr) => resolve({code: code, stdout : stdout, stderr : stderr}));
        });
    } catch (ex) {
        vscode.window.showErrorMessage(ex);
    }
}

async function isInstalled(name: string): Promise<Std> {
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

