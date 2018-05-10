import { injectable, inject } from 'inversify';
import { NotificationType } from 'vscode-jsonrpc';
import { TextDocumentIdentifier, Range } from 'vscode-base-languageclient/lib/base';
import { SetDecorationParams, EditorDecorationStyle, TextEditor, DeltaDecorationParams, EditorManager } from '@theia/editor/lib/browser';
import { ILanguageClient } from '@theia/languages/lib/common';
import { DiffUris } from '@theia/core/lib/browser/diff-uris';
import URI from '@theia/core/lib/common/uri';

const HIGHLIGHTS_NOTIFICATION_TYPE = new NotificationType<HighlightParams,void>("sts/highlight");

const BOOT_LIVE_HINTS = 'Boot-Live-Hints';

const INLINE_BOOT_HINT_DECORATION_STYLE = new EditorDecorationStyle('inline-boot-hint-decoration', style => {
    style.borderStyle = 'dotted';
    style.borderColor = '#32BA56';
    style.borderWidth = '1px';
});

const LINE_BOOT_HINT_DECORATION_STYLE = new EditorDecorationStyle('line-boot-hint-decoration', style => {
    style.backgroundImage = 'url(../../images/boot-icon.png)';
    style.display = 'block';
    style.width = '10px';
    style.height = '1em';
    style.margin = '0 2px 0 0';
});

@injectable()
export class HighlightService {

    protected readonly appliedDecorations = new Map<string, string[]>();

    constructor(
        @inject(EditorManager) protected readonly editorManager: EditorManager
    ) {}

    attach(client: ILanguageClient) {
        client.onNotification(HIGHLIGHTS_NOTIFICATION_TYPE, (params) => this.highlight(params));
    }

    async highlight(params: HighlightParams) {
        const editor = await this.findEditorByUri(params.doc.uri);
        if (editor) {
            const decorationParams: SetDecorationParams = {
                uri: params.doc.uri,
                kind: BOOT_LIVE_HINTS,
                newDecorations: params.ranges.map(r => {
                    return {
                        range: Range.create(r.start.line, r.start.character, r.end.line, r.end.character),
                        options: {
                            inlineClassName: INLINE_BOOT_HINT_DECORATION_STYLE.className,
                            glyphMarginClassName: LINE_BOOT_HINT_DECORATION_STYLE.className,
                            hoverMessage: 'Ho-ho, Boot Hint!',
                            isWholeLine: false
                        }
                    }
                })
            };
            const key = `${params.doc.uri}`;
            const oldDecorations = this.appliedDecorations.get(key) || [];
            const appliedDecorations = editor.deltaDecorations(<DeltaDecorationParams & SetDecorationParams>{oldDecorations, ...decorationParams});
            this.appliedDecorations.set(key, appliedDecorations);
        }
    }

    private async findEditorByUri(uri: string): Promise<TextEditor | undefined> {
        const editorWidget = await this.editorManager.getByUri(new URI(uri));
        if (editorWidget) {
            const editorUri = editorWidget.editor.uri;
            const openedInEditor = editorUri.toString() === uri;
            const openedInDiffEditor = DiffUris.isDiffUri(editorUri) && DiffUris.decode(editorUri).some(u => u.toString() === uri);
            if (openedInEditor || openedInDiffEditor) {
                return editorWidget.editor;
            }
        }
        return undefined;
    }


}

export interface HighlightParams {
    doc: TextDocumentIdentifier
    ranges: Range[]
}

