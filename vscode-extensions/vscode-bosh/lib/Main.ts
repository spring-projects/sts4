'use strict';
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below

import * as VSCode from 'vscode';
import * as commons from '@pivotal-tools/commons-vscode';
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
        extensionId: 'vscode-bosh',
        jvmHeap: "48m",
        workspaceOptions: VSCode.workspace.getConfiguration("bosh.ls"),
        explodedLsJarData: {
            lsLocation: 'language-server',
            mainClass: 'org.springframework.ide.vscode.bosh.BoshLanguageServerBootApp',
            configFileName: 'application.properties'
        },
        clientOptions: {
            documentSelector: [
                {
                    language: 'bosh-deployment-manifest',
                    scheme: 'file'
                },
                {
                    language: 'bosh-cloud-config',
                    scheme: 'file'
                }
            ],
            synchronize: {
                configurationSection: "bosh"
            }
        }
    };
    let clientPromise = commons.activate(options, context);
}

