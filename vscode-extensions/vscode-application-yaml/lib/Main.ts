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
        extensionId: 'vscode-application-yml',
        fatJarFile: 'target/vscode-application-yaml-0.0.1-SNAPSHOT.jar',
        clientOptions: {
            // HACK!!! documentSelector only takes string|string[] where string is language id, but DocumentFilter object is passed instead
            // Reasons:
            // 1. documentSelector is just passed over to functions like #registerHoverProvider(documentSelector, ...) that take documentSelector
            // parameter in string | DocumentFilter | string[] | DocumentFilter[] format
            // 2. Combination of non string|string[] documentSelector parameter and synchronize.textDocumentFilter function makes doc synchronization
            // events pass on to Language Server only for documents for which function passed via textDocumentFilter property return true

            // TODO: Remove <any> cast ones https://github.com/Microsoft/vscode-languageserver-node/issues/9 is resolved
            documentSelector: [ <any> {language: 'yaml', pattern: '**/application*.yml'}],
            synchronize: {
                // Synchronize the setting section to the server:
                configurationSection: 'languageServerExample',
                // Notify the server about file changes to 'javaconfig.json' files contain in the workspace
                fileEvents: [
                    //What's this for? Don't think it does anything useful for this example:
                    VSCode.workspace.createFileSystemWatcher('**/.clientrc')
                ],

                // TODO: Remove textDocumentFilter property ones https://github.com/Microsoft/vscode-languageserver-node/issues/9 is resolved
                textDocumentFilter: function(textDocument : TextDocument) : boolean {
                    let result : boolean =  /^(.*\/)?application[^\s\\/]*.yml$/i.test(textDocument.fileName);
                    return result;
                }
            }
        }
    };
    commons.activate(options, context);
}
