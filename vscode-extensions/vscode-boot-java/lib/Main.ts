'use strict';

import * as net from 'net';

import * as VSCode from 'vscode';
import * as Path from 'path';
import * as FS from 'fs';
import * as Net from 'net';
import * as ChildProcess from 'child_process';
import { LanguageClient, LanguageClientOptions, SettingMonitor, ServerOptions, StreamInfo } from 'vscode-languageclient';
import { workspace, TextDocument } from 'vscode';
import { Trace } from 'vscode-jsonrpc';

import * as commons from 'commons-vscode';
import { connect } from 'tls';

export function activate(context: VSCode.ExtensionContext) {
    let options: commons.ActivatorOptions = {
        DEBUG: false,
        CONNECT_TO_LS: false,
        extensionId: 'boot-java',
        launcher: (context: VSCode.ExtensionContext) => 'org.springframework.boot.loader.JarLauncher',
        classpath: (context: VSCode.ExtensionContext, jvm: commons.JVM) => {
            const classpath = [
                Path.resolve(context.extensionPath, 'jars/language-server.jar')
            ];
            if (!jvm.isJdk()) {
                VSCode.window.showWarningMessage('JAVA_HOME or PATH environment variable seems to point to a JRE. A JDK is required, hence Boot Hints are unavailable.');
            }
            const toolsJar = jvm.getToolsJar();
            if (toolsJar) {
                classpath.unshift(toolsJar);
            }
            return classpath;
        },
        clientOptions: {
            documentSelector: ['java'],
            synchronize: {
                configurationSection: 'boot-java'
            },
            initializationOptions: {
                workspaceFolders: workspace.workspaceFolders ? workspace.workspaceFolders.map(f => f.uri.toString()) : null
            },
        }
    };
    commons.activate(options, context);
}
