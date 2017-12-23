'use strict';
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below

import * as VSCode from 'vscode';
import * as commons from 'commons-vscode';
import * as Path from 'path';
import * as FS from 'fs';
import * as Net from 'net';
import * as ChildProcess from 'child_process';
import {LanguageClient, LanguageClientOptions, SettingMonitor, ServerOptions, StreamInfo} from 'vscode-languageclient';
import {TextDocument, OutputChannel} from 'vscode';

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

/** Called when extension is activated */
export function activate(context: VSCode.ExtensionContext) {
    let options : commons.ActivatorOptions = {
        DEBUG : false,
        CONNECT_TO_LS: false,
        extensionId: 'vscode-concourse',
        launcher: (context: VSCode.ExtensionContext) => Path.resolve(context.extensionPath, 'jars/language-server.jar'),
        jvmHeap: "48m",
        clientOptions: {
            documentSelector: [ PIPELINE_LANGUAGE_ID, TASK_LANGUAGE_ID ]
        }
    };
    let clientPromise = commons.activate(options, context);
}

