import { injectable, inject } from 'inversify';
import { VersionedTextDocumentIdentifier, Range, CodeLens } from 'vscode-languageserver-types';
import { EditorDecorationStyle, TextEditor, DeltaDecorationParams, EditorManager } from '@theia/editor/lib/browser';
import { DiffUris } from '@theia/core/lib/browser/diff-uris';
import URI from '@theia/core/lib/common/uri';

// const BOOT_LIVE_HINTS = 'Boot-Live-Hints';

const INLINE_BOOT_HINT_DECORATION_STYLE = new EditorDecorationStyle('inline-boot-hint-decoration', style => {
    style.backgroundColor = 'rgba(109,179,63,0.25)',
    style.borderColor = 'rgba(109,179,63,0.25)',
    style.borderSpacing = '4px',
    style.borderRadius = '4px',
    style.borderWidth = '4px'
});

@injectable()
export class HighlightService {

    protected readonly appliedDecorations = new Map<string, string[]>();

    constructor(
        @inject(EditorManager) protected readonly editorManager: EditorManager
    ) {}

    async handle(params: HighlightParams) {
        const editor = await this.findEditorByUri(params.doc.uri);
        if (editor) {
            const key = `${params.doc.uri}`;
            const decorationParams: DeltaDecorationParams = {
                // uri: params.doc.uri,
                // kind: BOOT_LIVE_HINTS,
                oldDecorations: this.appliedDecorations.get(key) || [],
                newDecorations: params.codeLenses.map(cl => {
                    return {
                        range: Range.create(cl.range.start.line, cl.range.start.character, cl.range.end.line, cl.range.end.character),
                        options: {
                            inlineClassName: INLINE_BOOT_HINT_DECORATION_STYLE.className,
                            isWholeLine: false
                        }
                    }
                })
            };
            const appliedDecorations = editor.deltaDecorations(decorationParams);
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
    doc: VersionedTextDocumentIdentifier;
    codeLenses: CodeLens[];
}

