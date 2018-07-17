import {StsAdapter, HighlightParams} from '@pivotal-tools/atom-languageclient-commons';
import {Convert} from 'atom-languageclient';
import { Range } from 'vscode-languageserver-protocol';
import {TextEditor, DecorationOptions } from 'atom';

const BOOT_HINT_GUTTER_NAME = 'boot-hint-gutter';

const DECORATION_OPTIONS: DecorationOptions = {
    type: 'highlight',
    class: 'boot-hint',
    // gutterName: BOOT_HINT_GUTTER_NAME
};


export class BootStsAdapter extends StsAdapter {

    constructor() {
        super();
    }

    onHighlight(params: HighlightParams) {
        this.findEditors(params.doc.uri).forEach(editor => this.markHintsForEditor(editor, params.ranges));
    }

    private markHintsForEditor(editor: TextEditor, ranges: Range[]) {
        editor.getDecorations(DECORATION_OPTIONS).map(decoration => decoration.getMarker()).forEach(m => m.destroy());
        editor.getDecorations({
            type: 'block',
            class: 'boot-hint-icon'
        }).map(decoration => decoration.getMarker()).forEach(m => m.destroy());
        if (Array.isArray(ranges)) {
            ranges.forEach(range => this.createHintMarker(editor, range));
        }
        const gutter = editor.gutterWithName(BOOT_HINT_GUTTER_NAME);
        if (gutter) {
            if (!ranges || !ranges.length) {
                gutter.hide();
            } else if (!gutter.isVisible()) {
                gutter.show();
            }
        }
    }

    private createHintMarker(editor: TextEditor, range: Range) {

        // Create marker model
        const marker = editor.markBufferRange(Convert.lsRangeToAtomRange(range));

        // Marker around the text in the editor
        editor.decorateMarker(marker, DECORATION_OPTIONS);

        const element = document.createElement('img');
        // element.textContent = '🐲';
        element.src = 'atom://spring-boot/styles/boot-icon.png';

        const AUX_DECORATION_OPTIONS: DecorationOptions = {
            type: 'block',
            position: 'before',
            item: element,
            class: 'boot-hint-icon'
        };
        const auxMarker = editor.markBufferRange(Convert.lsRangeToAtomRange({
            start: range.start,
            end: range.start
        }));

        editor.decorateMarker(auxMarker, AUX_DECORATION_OPTIONS);

        // Marker in the diagnostic gutter
        let gutter = editor.gutterWithName(BOOT_HINT_GUTTER_NAME);
        if (!gutter) {
            gutter = editor.addGutter({
                name: BOOT_HINT_GUTTER_NAME,
                visible: false,
            });
        }
        const iconElement = document.createElement('span');
        iconElement.setAttribute('class', 'gutter-boot-hint');
        gutter.decorateMarker(marker, {item: iconElement});
    }

}