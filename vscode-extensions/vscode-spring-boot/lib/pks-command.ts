'use strict';

import {hasResults, runCommand} from './commands-util';
import {runInTerminal} from './os-util';
import * as vscode from 'vscode';

export type StdResultHandler = (code: number, stdout: string, stderr: string) => void;



// NOTE: Be sure to add this under "contributes" in package.json to enable the command:
//
// "commands": [
//     {
//       "command": "pks.getCredentials",
//       "title": "PKS: Get Credentials"
//     }
//   ],
//
// AND ALSO, add activation event so that manifest extension activates when the PKS command is invoked:
// "activationEvents": [
//     "onCommand:pks.getCredentials",
//     "onLanguage:manifest-yaml"
//   ],
export function subscribePksCommand(context: vscode.ExtensionContext) {
    context.subscriptions.push(
        vscode.commands.registerCommand('pks.getCredentials', getCredentials)
    );
}

function run(command: string, handler: StdResultHandler) {
    let cmd = pksCli() + ' ' + command;
    runCommand(cmd, handler);
}

function pksCli(): string {
    return 'pks';
}

const getCredentials =  () => {
    run('clusters', async (code, stdout, stderr) => {
        if (hasResults(code, stdout, stderr)) {
            let results = stdout.split('\n');
            results = results.filter((l) => l.length > 0 && !l.startsWith("Name") && !l.startsWith('\n'));
            if (results.length > 0 ) {
                 let cluster =  await vscode.window.showQuickPick(results, { placeHolder: `Please select a cluster:` });
                 if (cluster) {
                    let clusterLineVals = cluster.match(/\S+/g) || [];
                    let clusterName = clusterLineVals.shift() ;            
                    run('get-credentials ' + clusterName, async (cd, out, err) => {
                            if (hasResults(cd, out, err)) {
                                runInTerminal('pks', 'kubectl cluster-info');
                            }
                        }
                    );
                 }
            }
        }
    });
   
};