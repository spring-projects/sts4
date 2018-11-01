'use strict';

import * as vscode from 'vscode';

// NOTE: be sure to add 'shelljs' to dependencies in package.json to enable building of PKS integration
// "dependencies": {
//     "@pivotal-tools/commons-vscode": "file:../commons-vscode/pivotal-tools-commons-vscode-0.2.2.tgz",
//     "shelljs": "^0.7.7",
//     "vscode-languageclient": "4.1.3"
//   },
import * as shelljs from 'shelljs';

export interface StdResult {
    readonly stdout: string;
    readonly stderr: string;
    readonly code: number;
}


export async function run(cmd: string): Promise<StdResult> {
    try {
        return await new Promise<StdResult>((resolve, reject) => {
            shelljs.exec(cmd, null, (code, stdout, stderr) => resolve({code: code, stdout : stdout, stderr : stderr}));
        });
    } catch (ex) {
        vscode.window.showErrorMessage(ex);
    }
}

async function isInstalled(name: string): Promise<StdResult> {
    let command = `which ${name}`;
    return await run(command);
}


// export function runCommand(cmd: string) {
//      run(cmd).then(standardResult => {
//         if (standardResult.code != 0 && standardResult.stderr) {
//             vscode.window.showErrorMessage(standardResult.stderr);
//         }

//     });
// }

export function hasResults(stdResult: StdResult): boolean {
    return stdResult && stdResult.code === 0 && stdResult.stdout != null;
}
