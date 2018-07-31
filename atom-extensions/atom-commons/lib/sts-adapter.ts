import {Convert} from 'atom-languageclient';
import {AtomEnvironment, TextEditor} from 'atom';
import {Position, CodeLens, TextDocumentIdentifier} from 'vscode-languageserver-protocol';

export class StsAdapter {

    constructor() {}

    findEditors(uri: string): TextEditor[] {
        const atomEnv: AtomEnvironment = atom;
        return atomEnv.workspace.getTextEditors()
            .filter(e => e && e.getPath() && Convert.pathToUri(e.getPath() || '') === uri);
    }

    onMoveCursor(params: CursorMovementParams): any {
        this.findEditors(params.uri).forEach(e => e.setCursorScreenPosition(Convert.positionToPoint(params.position)));
        return {applied: true};
    }

    onProgress(params: ProgressParams): void {}

    onHighlight(params: HighlightParams): void {}
}

export interface CursorMovementParams {
    readonly uri: string;
    readonly position: Position;
}

export interface ProgressParams {
    readonly id: string;
    readonly statusMsg: string;
}

export interface HighlightParams {
    readonly doc: TextDocumentIdentifier;
    readonly codeLenses: CodeLens[];
}
