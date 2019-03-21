/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
import { injectable, inject } from 'inversify';
import { CommandRegistry } from '@theia/core/lib/common';
import { ILanguageClient } from '@theia/languages/lib/browser';

const PROGRESS_NOTIFICATION_TYPE = 'sts/progress';

@injectable()
export class ProgressService {

    constructor(
        @inject(CommandRegistry) protected readonly commands: CommandRegistry
    ) {}

    attach(client: ILanguageClient) {
        client.onNotification(PROGRESS_NOTIFICATION_TYPE, params => this.progress(params));
    }

    private async progress(params: ProgressParams) {
    }

}

export interface ProgressParams {
    readonly id: string;
    readonly statusMsg: string;
}