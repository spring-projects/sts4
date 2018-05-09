import { injectable, inject } from 'inversify';
import { NotificationType } from 'vscode-jsonrpc';
import { TextDocumentIdentifier, Range } from 'vscode-base-languageclient/lib/base';
import { EditorDecorationsService, SetDecorationParams, EditorDecorationStyle } from '@theia/editor/lib/browser';

export const HIGHLIGHTS_NOTIFICATION_TYPE = new NotificationType<HighlightParams,void>("sts/highlight");

const BOOT_LIVE_HINTS = 'Boot-Live-Hints';

const INLINE_BOOT_HINT_DECORATION_STYLE = new EditorDecorationStyle('inline-boot-hint-decoration', style => {
    style.borderStyle = 'dotted';
    style.borderColor = '#32BA56';
    style.borderWidth = '1px';
});

// const LINE_BOOT_HINT_DECORATION_STYLE = new EditorDecorationStyle('line-boot-hint-decoration', style => {
//     style.content = '../../images/boot-icon.png';
//     style.display = 'block';
//     style.width = '10px';
//     style.height = '1em';
//     style.margin = '0 2px 0 0';
// });

@injectable()
export class HighlightService {

    constructor(
        @inject(EditorDecorationsService) protected readonly decorationService: EditorDecorationsService
    ) {}

    highlight(params: HighlightParams) {
        const decorationParams: SetDecorationParams = {
            uri: params.doc.uri,
            kind: BOOT_LIVE_HINTS,
            newDecorations: params.ranges.map(r => {
                return {
                    range: Range.create(r.start.line, r.start.character, r.end.line, r.end.character),
                    options: {
                        inlineClassName: INLINE_BOOT_HINT_DECORATION_STYLE.className,
                        // linesDecorationsClassName: LINE_BOOT_HINT_DECORATION_STYLE.className,
                        hoverMessage: 'Ho-ho, Boot Hint!',
                        isWholeLine: false
                    }
                }
            })
        };
        this.decorationService.setDecorations(decorationParams);

    }

}

export interface HighlightParams {
    doc: TextDocumentIdentifier
    ranges: Range[]
}

