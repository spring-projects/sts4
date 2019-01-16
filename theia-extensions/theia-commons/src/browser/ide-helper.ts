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