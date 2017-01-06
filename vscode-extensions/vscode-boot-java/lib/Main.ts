'use strict';
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below

import * as VSCode from 'vscode';
import * as Path from 'path';
import * as FS from 'fs';
import * as Net from 'net';
import * as ChildProcess from 'child_process';
import {LanguageClient, LanguageClientOptions, SettingMonitor, ServerOptions, StreamInfo} from 'vscode-languageclient';
import {TextDocument} from 'vscode';

import * as commons from 'commons-vscode';

/** Called when extension is activated */
export function activate(context: VSCode.ExtensionContext) {
    let options : commons.ActivatorOptions = {
        DEBUG: true,
        extensionId: 'vscode-boot-java',
        fatJarFile: 'target/vscode-boot-java-0.0.1-SNAPSHOT.jar',
        clientOptions: {
            // HACK!!! documentSelector only takes string|string[] where string is language id, but DocumentFilter object is passed instead
            // Reasons:
            // 1. documentSelector is just passed over to functions like #registerHoverProvider(documentSelector, ...) that take documentSelector
            // parameter in string | DocumentFilter | string[] | DocumentFilter[] format
            // 2. Combination of non string|string[] documentSelector parameter and synchronize.textDocumentFilter function makes doc synchronization
            // events pass on to Language Server only for documents for which function passed via textDocumentFilter property return true

            // TODO: Remove <any> cast ones https://github.com/Microsoft/vscode-languageserver-node/issues/9 is resolved
            documentSelector: ['java'],
            synchronize: {
                configurationSection: 'vscode-boot-java'
            }
        }
    };
    commons.activate(options, context);
}
