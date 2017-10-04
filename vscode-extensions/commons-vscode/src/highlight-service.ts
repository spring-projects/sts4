import {TextDocumentIdentifier, Position, Range} from 'vscode-languageclient'
import * as VSCode from 'vscode';
import * as path from "path";

function toDecoration(rng : Range) : VSCode.Range {
    return new VSCode.Range(toPosition(rng.start), toPosition(rng.end));
}

function toPosition(p : Position) : VSCode.Position {
    return new VSCode.Position(p.line, p.character);
}
 
export interface HighlightParams {
    doc: TextDocumentIdentifier
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
            //        textDecoration: "underline",
            gutterIconPath: path.resolve(__dirname, "../icons/boot-icon.png"),
            gutterIconSize: "contain",
            outline: "#32BA56 dotted thin"
        });
        this.highlights = new Map();
    }

    handle(params : HighlightParams) : void {
        this.highlights.set(params.doc.uri, params.ranges);
        this.refresh(params.doc.uri);
    }

    refresh(uri : String) {
        let editor = VSCode.window.activeTextEditor;
        let activeUri = editor.document.uri.toString();
        if (uri===activeUri) {
            //We only update highlights in the active editor for now
            let highlights : Range[] = this.highlights.get(uri) || [];
            let decorations = highlights.map(hl => toDecoration(hl));
            editor.setDecorations(this.DECORATION, decorations);
        }
    }
}