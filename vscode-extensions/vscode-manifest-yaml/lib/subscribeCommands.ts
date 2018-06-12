'use strict';

import * as vscode from 'vscode';
import * as pks from './pks';
import { runInTerminal } from './osutils';

// NOTE: Be sure to add this under "contributes" in package.json to enable the command:
//
// "commands": [
//     {
//       "command": "subscribeCommands.pksGetCredentials",
//       "title": "PKS: Get Credentials"
//     }
//   ],
//
// AND ALSO, add activation event so that manifest extension activates when the PKS command is invoked:
// "activationEvents": [
//     "onCommand:subscribeCommands.pksGetCredentials",
//     "onLanguage:manifest-yaml"
//   ],
export function subscribeCommands(context: vscode.ExtensionContext) {
    context.subscriptions.push(
        vscode.commands.registerCommand('subscribeCommands.pksGetCredentials', getCredentials)
    );
}

function hasResults(code: number, stdout: string, stderr: string): boolean {
    return code === 0 && stdout != null;
}

const getCredentials =  () => {
    pks.runPks('clusters', async (code, stdout, stderr) => {
        if (hasResults(code, stdout, stderr)) {
            let results = stdout.split('\n');
            results = results.filter((l) => l.length > 0 && !l.startsWith("Name") && !l.startsWith('\n'));
            if (results.length > 0 ) {
                 let cluster =  await vscode.window.showQuickPick(results, { placeHolder: `Please select a cluster:` });
                 if (cluster) {
                    let clusterLineVals = cluster.match(/\S+/g) || [];
                    let clusterName = clusterLineVals.shift() ;            
                    pks.runPks('get-credentials ' + clusterName, async (cd, out, err) => {
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