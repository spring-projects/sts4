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
import { injectable } from 'inversify';
import * as deepEqual from 'deep-equal';
import {HighlightParams} from './highlight-service';
import * as Lsp from 'vscode-languageserver-types';

@injectable()
export class HighlightCodeLensService implements monaco.languages.CodeLensProvider {

    private highlights : Map<string, HighlightParams> = new Map<string, HighlightParams>();

    private _onDidChangeCodeLenses = new monaco.Emitter<this>();

    public get onDidChange(): monaco.IEvent<this> {
        return this._onDidChangeCodeLenses.event;
    }

    handle(highlghtParams: HighlightParams) {
        if (!deepEqual(this.highlights.get(highlghtParams.doc.uri), highlghtParams)) {
            this.highlights.set(highlghtParams.doc.uri, highlghtParams);
            this._onDidChangeCodeLenses.fire();
        }
    }

    static toMonacoCodeLens(cl: Lsp.CodeLens): monaco.languages.ICodeLensSymbol {
        const codeLens: monaco.languages.ICodeLensSymbol = {
            range: {
                startLineNumber: cl.range.start.line + 1,
                startColumn: cl.range.start.character,
                endLineNumber: cl.range.end.line + 1,
                endColumn: cl.range.end.character
            },
            command: {
                id: cl.command.command,
                title: cl.command.title,
                arguments: cl.command.arguments
            }
        };
        return codeLens;
    }

    provideCodeLenses(document: monaco.editor.ITextModel, token: monaco.CancellationToken) {
        const activeUri = document.uri.toString();
        const activeVersion = document.getVersionId();
        const highlightParams = this.highlights.get(activeUri);
        if (highlightParams && highlightParams.doc.version === activeVersion) {
            const codeLenses = highlightParams.codeLenses || [];
            return codeLenses.filter(cl => cl.command).map(cl => HighlightCodeLensService.toMonacoCodeLens(cl));
        }
        return [];
    };

    resolveCodeLens(model: monaco.editor.ITextModel, codeLens: monaco.languages.ICodeLensSymbol, token: monaco.CancellationToken) {
        return codeLens;
    }

}
