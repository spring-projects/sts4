import {Convert} from 'atom-languageclient';

export class StsAdapter {

    constructor() {}

    findEditors(uri) {
        return atom.workspace.getTextEditors()
            .filter(e => e && e.getPath() && Convert.pathToUri(e.getPath()) === uri);
    }

    onMoveCursor(params) {
        findEditors(params.uri).forEach(e => e.setCursorScreenPosition(Convert.positionToPoint(params.position)));
        return { applied: true};
    }

    onProgress(params) {}

    onHighlight(params) {}
}
