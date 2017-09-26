import {Convert} from 'atom-languageclient';

export class StsAdapter {

    // Public: Attach to a {StsLanguageClientConnection} to receive messages.
    static attach(connection) {
        connection._onRequest({method: 'sts/moveCursor'}, params => StsAdapter.onMoveCursor(params));
        connection._onNotification({method: 'sts/progress'}, params => StsAdapter.onProgress(params));
    }

    static onMoveCursor(params) {
        atom.workspace.getTextEditors()
            .filter(e => Convert.pathToUri(e.getPath()) === params.uri)
            .forEach(e => e.setCursorScreenPosition(Convert.positionToPoint(params.position)));
        return { applied: true};
    }

    static onProgress(params) {
        console.log('PROGRESS: ' + JSON.stringify(params));
    }
}
