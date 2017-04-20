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
        extensionId: 'vscode-manifest-yaml',
        fatJarFile: 'jars/language-server.jar',
        jvmHeap: '64m',
        clientOptions: {
            // HACK!!! documentSelector only takes string|string[] where string is language id, but DocumentFilter object is passed instead
            // Reasons:
            // 1. documentSelector is just passed over to functions like #registerHoverProvider(documentSelector, ...) that take documentSelector
            // parameter in string | DocumentFilter | string[] | DocumentFilter[] format
            // 2. Combination of non string|string[] documentSelector parameter and synchronize.textDocumentFilter function makes doc synchronization
            // events pass on to Language Server only for documents for which function passed via textDocumentFilter property return true

            // TODO: Remove <any> cast ones https://github.com/Microsoft/vscode-languageserver-node/issues/9 is resolved
            documentSelector: ["manifest-yaml"]
        }
    };
    commons.activate(options, context);
}

