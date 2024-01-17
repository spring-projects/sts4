// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

"use strict";
import * as vscode from "vscode";
import { Cli } from "./cli/core";
import { handleNew } from "./boot-project/boot-project";

export const CLI = new Cli();

export async function activate(context: vscode.ExtensionContext): Promise<void> {
    vscode.commands.registerCommand('vscode-spring-cli.new-project', () => {
        handleNew({});
    });
}

export async function deactivate(): Promise<void> {
}
