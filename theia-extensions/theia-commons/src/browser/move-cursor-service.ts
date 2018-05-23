import { injectable, inject } from 'inversify';
import { RequestType } from 'vscode-jsonrpc';
import { Position } from 'vscode-base-languageclient/lib/base';
import { CommandRegistry } from '@theia/core/lib/common';
import { ILanguageClient } from '@theia/languages/lib/common';
import {IdeHelper} from './ide-helper';
import {EditorManager} from '@theia/editor/lib/browser';

export const MOVE_CURSOR_REQUEST_TYPE = new RequestType<CursorMovementParams, {}, void, void>('sts/moveCursorr');

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
