import * as path from 'path';
import * as vscode from 'vscode';
import * as SockJS from 'sockjs-client';
import * as WebSocket from 'ws';
import {LanguageClient, NotificationType} from "vscode-languageclient";

export async function activate(context: vscode.ExtensionContext, client: LanguageClient) {

    context.subscriptions.push(
		vscode.commands.registerCommand('sts4.liveBeans.start', () => {
            LiveBeansView.createOrShow(context.extensionPath, client);
		})
	);

}

/**
 * Manages cat coding webview panels
 */
class LiveBeansView {
	/**
	 * Track the currently panel. Only allow a single panel to exist at a time.
	 */
	public static currentPanel: LiveBeansView | undefined;

	public static readonly viewType = 'catCoding';

	private readonly _panel: vscode.WebviewPanel;
	private readonly _extensionPath: string;
    private _disposables: vscode.Disposable[] = [];

	public static createOrShow(extensionPath: string, client: LanguageClient) {
		const column = vscode.window.activeTextEditor
			? vscode.window.activeTextEditor.viewColumn
			: undefined;

		// If we already have a panel, show it.
		if (LiveBeansView.currentPanel) {
			LiveBeansView.currentPanel._panel.reveal(column);
			return;
		}

		// Otherwise, create a new panel.
		const panel = vscode.window.createWebviewPanel(
			LiveBeansView.viewType,
			'Live Beans',
			column || vscode.ViewColumn.One,
			{
				// Enable javascript in the webview
                enableScripts: true,
                


				// And restrict the webview to only loading content from our extension's `media` directory.
				localResourceRoots: [vscode.Uri.file(path.join(extensionPath, 'media'))]
			}
		);

        LiveBeansView.currentPanel = new LiveBeansView(panel, extensionPath);

		console.log('Created webview panel!');
		const bridge: LSWebViewToLSPBridge = new LSWebViewToLSPBridge(panel.webview, client);
        panel.webview.onDidReceiveMessage(message => bridge.sendToLs(message));
	}

	public static revive(panel: vscode.WebviewPanel, extensionPath: string) {
		LiveBeansView.currentPanel = new LiveBeansView(panel, extensionPath);
	}

	private constructor(panel: vscode.WebviewPanel, extensionPath: string) {
		this._panel = panel;
		this._extensionPath = extensionPath;

		// Set the webview's initial html content
		this._update();

		// Listen for when the panel is disposed
		// This happens when the user closes the panel or when the panel is closed programatically
		this._panel.onDidDispose(() => this.dispose(), null, this._disposables);

		// Update the content based on view changes
		this._panel.onDidChangeViewState(
			e => {
				if (this._panel.visible) {
					this._update();
				}
			},
			null,
			this._disposables
		);

		// Handle messages from the webview
		this._panel.webview.onDidReceiveMessage(
			message => {
				switch (message.command) {
					case 'alert':
						vscode.window.showErrorMessage(message.text);
						return;
				}
			},
			null,
			this._disposables
		);
	}

	public doRefactor() {
		// Send a message to the webview webview.
		// You can send any JSON serializable data.
		this._panel.webview.postMessage({ command: 'refactor' });
	}

	public dispose() {
		LiveBeansView.currentPanel = undefined;

		// Clean up our resources
		this._panel.dispose();

		while (this._disposables.length) {
			const x = this._disposables.pop();
			if (x) {
				x.dispose();
			}
		}
	}

	private _update() {
		this._panel.webview.html = this._getHtmlForWebview(/*cats[catName]*/);
	}

	mediaUrl(...pathsegments: string[]) {
		const mediaPath = path.join(this._extensionPath, 'media')

		// Local path to main script run in the webview
		const scriptPathOnDisk = vscode.Uri.file(
			path.join(mediaPath, ...pathsegments)
		);

		// And the uri we use to load this script in the webview
		return scriptPathOnDisk.with({ scheme: 'vscode-resource' });
	}

	private _getHtmlForWebview(/*catGif: string*/) {
        
		// And the uri we use to load this script in the webview
		const scriptUri = this.mediaUrl('bundle.js');
        const cssUri = this.mediaUrl('css', 'page.css');
        
		// Use a nonce to whitelist which scripts can be run
        const nonce = getNonce();
        
		return `<!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>sprotty Circles Example</title>
            <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.2.1/css/bootstrap.min.css">    
            <link rel="stylesheet" href=${cssUri}>
            <!-- support Microsoft browsers -->
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/dom4/3.0.0/dom4.js">
        </head>
        <body>
            <div class="container">
                <div class="row" id="sprotty-app" data-app="circlegraph">
                    <div class="col-md-10">
                        <h1>sprotty Circles Example</h1>
                        <p>
                            <button id="refresh">Refresh</button>
                            <button id="scrambleNodes">Scramble nodes</button>
                        </p>
                    </div>
                    <div class="help col-md-2">
                        <a href='https://github.com/theia-ide/sprotty/wiki/Using-sprotty'>Help</a>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-12">
                        <div id="spring-boot" class="sprotty"/>
                    </div>
                    <div class="copyright">
                        &copy; 2017 <a href="http://typefox.io">TypeFox GmbH</a>.
                    </div>
                </div>
            </div>
        <script src="${scriptUri}"></script>
        </body>
        </html>`;
	}
}

function getNonce() {
	let text = '';
	const possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
	for (let i = 0; i < 32; i++) {
		text += possible.charAt(Math.floor(Math.random() * possible.length));
	}
	return text;
}

const SOCKET_MESSAGE_BUFFER = 4000;
const END_MESSAGE = '@end';

interface LSWebviewBridgeConstructor {
	create(webview: vscode.Webview): LSWebViewBridge;
}

interface LSWebViewBridge {
    sendToLs(message: any);
}

class LSWebViewToWebsocketBridge implements LSWebViewBridge {
	private ws: WebSocket;

	constructor(webview: vscode.Webview): LSWebViewBridge {
		this.ws = new SockJS('http://localhost:8080/websocket');
		this.ws.addEventListener('message', (event) => {
			console.dir(event.data);
			const actionMessage = JSON.parse(event.data);
			webview.postMessage(actionMessage);
		});
	}

	sendToLs(message: any) {
		console.log('Send messge Webview -> LS');
		const messageStr = JSON.stringify(message);
		for (let offset = 0; offset < messageStr.length; offset += SOCKET_MESSAGE_BUFFER) {
			this.ws.send(messageStr.substring(offset, offset + SOCKET_MESSAGE_BUFFER));
		}
		this.ws.send(END_MESSAGE);
	}

}

const SPROTTY_LSP_NOTIFICATION =  new NotificationType<any, void>("sts/sprotty");

class LSWebViewToLSPBridge implements LSWebViewBridge {

	private client: LanguageClient;

    constructor(webview: vscode.Webview, client: LanguageClient) {
        client.onReady().then(() => {
            client.onNotification(SPROTTY_LSP_NOTIFICATION, async (params: any) =>
                webview.postMessage(params)
        	);
        });
		this.client = client;

    }

    sendToLs(message: any) {
    	this.client.onReady().then(() => {
    		this.client.sendNotification(SPROTTY_LSP_NOTIFICATION, message);
		});
    }
}
