/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
import { injectable, inject } from 'inversify';
import { CommandRegistry } from '@theia/core/lib/common';
import { ILanguageClient } from '@theia/languages/lib/browser';

export const ADD_LISTENER_REQUEST_TYPE = 'sts/addClasspathListener';
export const REMOVE_LISTENER_REQUEST_TYPE ='sts/removeClasspathListener';

@injectable()
export class ClasspathService {

    constructor(
        @inject(CommandRegistry) protected readonly commands: CommandRegistry
    ) {}

    attach(client: ILanguageClient) {
        client.onRequest(ADD_LISTENER_REQUEST_TYPE, params => this.addListener(params));
        client.onRequest(REMOVE_LISTENER_REQUEST_TYPE, params => this.removeListener(params));
    }

    private async addListener(params: ClasspathListenerParams) {
        this.commands.executeCommand(/*'java.execute.workspaceCommand',*/ 'sts.java.addClasspathListener', params.callbackCommandId);
    }

    private async removeListener(params: ClasspathListenerParams) {
        this.commands.executeCommand(/*'java.execute.workspaceCommand',*/ 'sts.java.removeClasspathListener', params.callbackCommandId);
    }
}

export interface ClasspathListenerParams {
    callbackCommandId: string
}
