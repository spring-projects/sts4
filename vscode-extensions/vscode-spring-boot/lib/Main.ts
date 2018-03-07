'use strict';
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below

import * as VSCode from 'vscode';
import * as Path from 'path';
import * as FS from 'fs';
import * as Net from 'net';
import * as ChildProcess from 'child_process';
import {LanguageClient, LanguageClientOptions, SettingMonitor, ServerOptions, StreamInfo} from 'vscode-languageclient';
import { workspace, TextDocument } from 'vscode';

import * as commons from 'commons-vscode';

const PROPERTIES_LANGUAGE_ID = "spring-boot-properties";
const YAML_LANGUAGE_ID = "spring-boot-properties-yaml";
const JAVA_LANGUAGE_ID = "java";

/** Called when extension is activated */
export function activate(context: VSCode.ExtensionContext) {
    let options : commons.ActivatorOptions = {
        DEBUG: false,
        CONNECT_TO_LS: false,
        extensionId: 'vscode-spring-boot',
        preferJdk: true,
        checkjvm: (context: VSCode.ExtensionContext, jvm: commons.JVM) => {
            if (!jvm.isJdk()) {
                VSCode.window.showWarningMessage('JAVA_HOME or PATH environment variable seems to point to a JRE. A JDK is required, hence Boot Hints are unavailable.');
            }
        },
        clientOptions: {
            documentSelector: [ PROPERTIES_LANGUAGE_ID, YAML_LANGUAGE_ID, JAVA_LANGUAGE_ID ],
            synchronize: {
                configurationSection: 'boot-java'
            },
            initializationOptions: {
                workspaceFolders: workspace.workspaceFolders ? workspace.workspaceFolders.map(f => f.uri.toString()) : null
            }
        }
    };
    setTimeout(() => {
        VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.resolveClasspath", "Brocoli", "Cabbage")
        .then(
            (commandResult) => VSCode.window.showInformationMessage(""+commandResult),
            (e) => {
                VSCode.window.showErrorMessage("Error" + e);
            }
        );
    }, 10000);

    return commons.activate(options, context);
}
