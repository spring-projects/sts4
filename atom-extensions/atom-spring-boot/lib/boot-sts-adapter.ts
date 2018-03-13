import {StsAdapter, HighlightParams} from '@pivotal-tools/atom-languageclient-commons';
import {Convert} from 'atom-languageclient';
import { Range } from 'vscode-languageserver-protocol';
import {TextEditor} from 'atom';

const BOOT_DATA_MARKER_TYPE: any = 'BootApp-Hint';
const BOOT_HINT_GUTTER_NAME = 'boot-hint-gutter';

export class BootStsAdapter extends StsAdapter {

    constructor() {
        super();
    }

    onHighlight(params: HighlightParams) {
        this.findEditors(params.doc.uri).forEach(editor => this.markHintsForEditor(editor, params.ranges));
    }

    private markHintsForEditor(editor: TextEditor, ranges: Range[]) {
        editor.findMarkers(BOOT_DATA_MARKER_TYPE).forEach(m => m.destroy());
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
        const marker = editor.markBufferRange(Convert.lsRangeToAtomRange(range), BOOT_DATA_MARKER_TYPE);

        // Marker around the text in the editor
        editor.decorateMarker(marker, {
            type: 'highlight',
            class: 'boot-hint'
        });

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