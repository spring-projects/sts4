'use strict';
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below

import * as VSCode from 'vscode';
import * as commons from 'commons-vscode';
import * as Path from 'path';
import * as FS from 'fs';
import * as Net from 'net';
import * as ChildProcess from 'child_process';
import {LanguageClient, RequestType, LanguageClientOptions, SettingMonitor, ServerOptions, StreamInfo} from 'vscode-languageclient';
import {TextDocument, OutputChannel} from 'vscode';
import {WorkspaceEdit} from 'vscode-languageserver-types';
import * as p2c from 'vscode-languageclient/lib/protocolConverter';

var log_output : OutputChannel = null;

const PIPELINE_LANGUAGE_ID = "concourse-pipeline-yaml";
const TASK_LANGUAGE_ID = "concourse-task-yaml";

function log(msg : string) {
    if (log_output) {
        log_output.append(msg +"\n");
    }
}

function error(msg : string) {
    if (log_output) {
        log_output.append("ERR: "+msg+"\n");
    }
}

interface QuickfixRequest {
    type: string;
    params: any;
}

/** Called when extension is activated */
export function activate(context: VSCode.ExtensionContext) {
    let commands = VSCode.commands
    let options : commons.ActivatorOptions = {
        DEBUG : false,
        CONNECT_TO_LS: false,
        extensionId: 'vscode-concourse',
        fatJarFile: 'jars/language-server.jar',
        jvmHeap: "48m",
        clientOptions: {
            documentSelector: [ PIPELINE_LANGUAGE_ID, TASK_LANGUAGE_ID ],
            synchronize: {
                // TODO: Remove textDocumentFilter property once https://github.com/Microsoft/vscode-languageserver-node/issues/9 is resolved
                textDocumentFilter: function(textDocument : TextDocument) : boolean {
                    let languageId = textDocument.languageId;
                    return  PIPELINE_LANGUAGE_ID===languageId || TASK_LANGUAGE_ID===languageId;
                }
            }
        }
    };
    let clientPromise = commons.activate(options, context);
    commands.registerCommand("sts.quickfix", (fixType, fixParams) => {
        return clientPromise.then(client => {
            let type : RequestType<QuickfixRequest, WorkspaceEdit, void> = {method : "sts/quickfix"};
            let params : QuickfixRequest = {type: fixType, params: fixParams};
            return client.sendRequest(type, params)
            .then(
                (edit) => { 
                    return VSCode.workspace.applyEdit(p2c.asWorkspaceEdit(edit)) 
                },
                (error) => { 
                    return VSCode.window.showErrorMessage(""+error) 
                }
            );
        })
    });
}

