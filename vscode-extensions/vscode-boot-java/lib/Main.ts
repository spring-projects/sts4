'use strict';
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below

import * as net from 'net';

import * as VSCode from 'vscode';
import * as Path from 'path';
import * as FS from 'fs';
import * as Net from 'net';
import * as ChildProcess from 'child_process';
import { LanguageClient, LanguageClientOptions, SettingMonitor, ServerOptions, StreamInfo } from 'vscode-languageclient';
import { TextDocument } from 'vscode';
import { Trace } from 'vscode-jsonrpc';

import * as commons from 'commons-vscode';

export function activate(context: VSCode.ExtensionContext) {

    let CONNECT_TO_LS = false;

    if (CONNECT_TO_LS) {
        let connectionInfo = {
            port: 5007
        };
        let serverOptions = () => {
            let socket = net.connect(connectionInfo);
            let result: StreamInfo = {
                writer: socket,
                reader: socket
            };
            return Promise.resolve(result);
        };

        let clientOptions: LanguageClientOptions = {
            documentSelector: ['java'],
            synchronize: {
                configurationSection: 'vscode-boot-java'
            }
        };

        let lc = new LanguageClient('vscode-boot-java', serverOptions, clientOptions);
        lc.trace = Trace.Verbose;
        let disposable = lc.start();
        context.subscriptions.push(disposable);
    }
    else {
        let options: commons.ActivatorOptions = {
            DEBUG: false,
            extensionId: 'vscode-boot-java',
            fatJarFile: 'target/vscode-boot-java-0.0.1-SNAPSHOT.jar',
            clientOptions: {
                documentSelector: ['java'],
                synchronize: {
                    configurationSection: 'vscode-boot-java'
                }
            }
        };
        commons.activate(options, context);
    }
}
