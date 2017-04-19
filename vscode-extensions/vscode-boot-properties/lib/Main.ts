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
        DEBUG: false,
        CONNECT_TO_LS: false,
        extensionId: 'vscode-boot-properties',
        fatJarFile: 'jars/language-server.jar',
        clientOptions: {
            // HACK!!! documentSelector only takes string|string[] where string is language id, but DocumentFilter object is passed instead
            // Reasons:
            // 1. documentSelector is just passed over to functions like #registerHoverProvider(documentSelector, ...) that take documentSelector
            // parameter in string | DocumentFilter | string[] | DocumentFilter[] format
            // 2. Combination of non string|string[] documentSelector parameter and synchronize.textDocumentFilter function makes doc synchronization
            // events pass on to Language Server only for documents for which function passed via textDocumentFilter property return true

            // TODO: Remove <any> cast ones https://github.com/Microsoft/vscode-languageserver-node/issues/9 is resolved
            documentSelector: [
                // for application.properties files
                 <any> {language: 'ini', pattern: '**/application*.properties'},
                 <any> {language: 'java-properties', pattern: '**/application*.properties'},
                 <any> {language: 'properties', pattern: '**/application*.properties'},
                 // for application.yml files
                 <any> {language: 'yaml', pattern: '**/application*.yml'}
                 
            ]
        }
    };
    commons.activate(options, context);
}
