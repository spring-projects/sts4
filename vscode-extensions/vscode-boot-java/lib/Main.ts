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
import { Range, TextDocument } from 'vscode';
import { Trace } from 'vscode-jsonrpc';

import * as commons from 'commons-vscode';

const MARK_WORD = "boot";

export function activate(context: VSCode.ExtensionContext) {

    let options: commons.ActivatorOptions = {
        DEBUG: false,
        CONNECT_TO_LS: true,
        extensionId: 'vscode-boot-java',
        fatJarFile: 'jars/language-server.jar',
        clientOptions: {
            documentSelector: ['java'],
            synchronize: {
                configurationSection: 'vscode-boot-java'
            }
        }
    };

    const DECORATION = VSCode.window.createTextEditorDecorationType({
//        textDecoration: "underline",
        gutterIconPath: "/home/kdvolder/git/spring-ide/plugins/org.springframework.ide.eclipse.boot/resources/icons/boot-icon.png",
        gutterIconSize: "contain",
        outline: "#BFBF3F dotted thin"
    });
    context.subscriptions.push(DECORATION);

    context.subscriptions.push(VSCode.workspace.onDidChangeTextDocument(e => {
        let editor = VSCode.window.activeTextEditor;
        if (editor && editor.document == e.document) {
            let text = e.document.getText();
            let toMark = text.indexOf(MARK_WORD);
            if (toMark>=0) {
                editor.setDecorations(DECORATION, [new Range(
                    editor.document.positionAt(toMark),
                    editor.document.positionAt(toMark+MARK_WORD.length)
                )]);
            } else {
                editor.setDecorations(DECORATION, []);
            }
        }
    }));

    commons.activate(options, context);
}
