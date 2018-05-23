import {EditorManager, TextEditor} from '@theia/editor/lib/browser';
import {DiffUris} from '@theia/core/lib/browser';
import URI from '@theia/core/lib/common/uri';

export class IdeHelper {

    static async findEditorByUri(editorManager: EditorManager, uri: string): Promise<TextEditor | undefined> {
        const editorWidget = await editorManager.getByUri(new URI(uri));
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