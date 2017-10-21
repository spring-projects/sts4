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
        extensionId: 'boot-java',
        launcher: (context: VSCode.ExtensionContext) => 'org.springframework.boot.loader.JarLauncher',
        classpath: (context: VSCode.ExtensionContext) => {
            const classpath = [
                Path.resolve(context.extensionPath, 'jars/language-server.jar')
            ];
            const toolsJar = commons.findJvmFile('lib', 'tools.jar');
            if (toolsJar) {
                classpath.push(toolsJar);
            } else {
                VSCode.window.showWarningMessage('JAVA_HOME environment variable points either to JRE or JDK missing "lib/tools.jar" hence Boot Hints are unavailable');
            }
            return classpath;
        },
        clientOptions: {
            documentSelector: ['java'],
            synchronize: {
                configurationSection: 'boot-java'
            }
        }
    };
    commons.activate(options, context);
}
