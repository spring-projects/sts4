import {ManhattanEdgeRouter, RectangularNode, SEdge, WebSocketDiagramServer, ActionMessage, DiagramServer} from "sprotty";
import {injectable} from 'inversify';

export class BeanNode extends RectangularNode {

}

export class OrthogonalEgde extends SEdge {

    routerKind = ManhattanEdgeRouter.KIND;

}

const SOCKET_MESSAGE_BUFFER = 4000;
const END_MESSAGE = '@end';

@injectable()
export class ExampleWebsocketDiagramServer extends WebSocketDiagramServer {

    protected sendMessage(message: ActionMessage): void {
        if (this.webSocket) {
            const messageStr = JSON.stringify(message);
            let offset = 0;
            while (offset < messageStr.length) {
                this.webSocket.send(messageStr.substring(offset, offset + SOCKET_MESSAGE_BUFFER));
                offset += SOCKET_MESSAGE_BUFFER;
            }
            this.webSocket.send(END_MESSAGE);
        } else {
            throw new Error('WebSocket is not connected');
        }
    }
}

@injectable()
export class VSCodeWebViewDiagramServer extends DiagramServer {

    protected vscode: any;

    listen(vscode: any): void {
        console.log('Adding listener!');
        window.addEventListener('message', (event: any) => {
            const message = event.data; // The json data that the extension sent
            if (event.data == 'test') {
                console.log('Received TEST message!');
            } else {
                console.log('Message Received');
                this.messageReceived(message);
            }
        });

        this.vscode = vscode;

    }

    // disconnect() {
    //     if (this.webSocket) {
    //         this.webSocket.close();
    //         this.webSocket = undefined;
    //     }
    // }
    //
    protected sendMessage(message: ActionMessage): void {
        console.log('Send Message');
        if (this.vscode) {
            this.vscode.postMessage(message);
        } else {
            throw new Error('No VSCode api object');
        }
    }
}
