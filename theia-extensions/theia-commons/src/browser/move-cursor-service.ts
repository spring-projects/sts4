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
import { Position } from 'vscode-languageserver-types';
import { CommandRegistry } from '@theia/core/lib/common';
import { ILanguageClient } from '@theia/languages/lib/browser';
import {IdeHelper} from './ide-helper';
import {EditorManager} from '@theia/editor/lib/browser';

export const MOVE_CURSOR_REQUEST_TYPE = 'sts/moveCursorr';

@injectable()
export class MoveCursorService {

    constructor(
        @inject(CommandRegistry) protected readonly commands: CommandRegistry,
        @inject(EditorManager) private readonly editorManager: EditorManager
    ) {}

    attach(client: ILanguageClient) {
        client.onRequest(MOVE_CURSOR_REQUEST_TYPE, params => this.moveCursor(params));
    }

    private async moveCursor(params: CursorMovementParams) {
        const editor = await IdeHelper.findEditorByUri(this.editorManager, params.uri);
        if (editor) {
            editor.cursor = Position.create(params.position.line, params.position.character);
        }
        return { applied: true };
    }

}

export interface CursorMovementParams {
    readonly uri: string;
    readonly position: Position;
}
