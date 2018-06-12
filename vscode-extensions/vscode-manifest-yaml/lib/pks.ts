'use strict';

import * as osUtils from './osutils';
import * as vscode from 'vscode';

export type StdResultHandler = (code: number, stdout: string, stderr: string) => void;

export function runPks(command: string, handler: StdResultHandler) {
    let cmd = pksCli() + ' ' + command;
    runCommand(cmd, handler);
}

export function runCommand(cmd: string, handler: StdResultHandler) {
    osUtils.run(cmd).then(({ code, stdout, stderr }) => {
        if (code != 0 && stderr) {
            vscode.window.showErrorMessage(stderr);
        }
        else {
            handler(code, stdout, stderr);
        }
    });
}

function pksCli(): string {
    return 'pks';
}
