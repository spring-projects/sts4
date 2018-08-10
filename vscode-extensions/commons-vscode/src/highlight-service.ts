import {VersionedTextDocumentIdentifier, Position, Range} from 'vscode-languageclient'
import * as VSCode from 'vscode';
import * as path from "path";

function toDecoration(rng : Range) : VSCode.Range {
    return new VSCode.Range(toPosition(rng.start), toPosition(rng.end));
}

function toPosition(p : Position) : VSCode.Position {
    return new VSCode.Position(p.line, p.character);
}
 
export interface HighlightParams {
    doc: VersionedTextDocumentIdentifier
    ranges: Range[]
}

export class HighlightService {

    DECORATION : VSCode.TextEditorDecorationType;

    highlights : Map<String, Range[]>;

    dispose() {
        this.DECORATION.dispose();
    }

    constructor() {
        this.DECORATION = VSCode.window.createTextEditorDecorationType({
            // before: {
            //     contentIconPath: path.resolve(__dirname, "../icons/boot-12h.png"),
            //     margin: '2px 2px 0px 0px'
            // },
            backgroundColor: 'rgba(109,179,63,0.25)',
            borderColor: 'rgba(109,179,63,0.25)',
            borderSpacing: '4px',
            borderRadius: '4px',
            borderWidth: '4px'
        });
        this.highlights = new Map();
    }

    handle(params : HighlightParams) : void {
        this.highlights.set(params.doc.uri, params.ranges);
        this.refresh(params.doc);
    }

    refresh(docId: VersionedTextDocumentIdentifier) {
        let editors = VSCode.window.visibleTextEditors;
        for (let editor of editors) {
            const activeUri = editor.document.uri.toString();
            const activeVersion = editor.document.version;
            if (docId.uri === activeUri && docId.version === activeVersion) {
                //We only update highlights in the active editor for now
                let highlights : Range[] = this.highlights.get(docId.uri) || [];
                let decorations = highlights.map(hl => toDecoration(hl));
                editor.setDecorations(this.DECORATION, decorations);
            }
        }
    }
}