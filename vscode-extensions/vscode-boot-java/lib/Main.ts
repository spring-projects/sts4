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

    let options: commons.ActivatorOptions = {
        DEBUG: false,
        CONNECT_TO_LS: false,
        extensionId: 'vscode-boot-java',
        fatJarFile: 'jars/language-server.jar',
        clientOptions: {
            documentSelector: ['java'],
            synchronize: {
                configurationSection: 'vscode-boot-java'
            }
        }
    };
    commons.activate(options, context);
}
