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

const PROPERTIES_LANGUAGE_ID = "spring-boot-properties";
const YAML_LANGUAGE_ID = "spring-boot-properties-yaml";

/** Called when extension is activated */
export function activate(context: VSCode.ExtensionContext) {
    VSCode.window.showInformationMessage(
        "The `vscode-boot-properties` extension is obsolete and no longer functional. "+
        "Please uninstall it and install the `vscode-spring-boot` extension instead."
    );
}
