import * as path from 'path';
import * as vscode from 'vscode';
import * as SockJS from 'sockjs-client';
import * as WebSocket from 'ws';
import {LanguageClient, NotificationType} from "vscode-languageclient";

export async function activate(context: vscode.ExtensionContext, client: LanguageClient) {

    context.subscriptions.push(
		vscode.commands.registerCommand('sts4.liveBeans.start', () => {
            vscode.window.showQuickPick([
            	'process-1',
            	'process-2',
            	'process-3',
            ]).then(pick => {
            	console.log('Pick is: ' + pick);
            	LiveBeansView.createOrShow(context.extensionPath, client, pick, pick);


            })
		})
	);

}

/**
 * Manages cat coding webview panels
 */
class LiveBeansView {

	/**
	 * Track panels that currently exist. Indexed by process id.
	 */
	public static currentPanels: Map<string, LiveBeansView> = new Map();

	public static readonly viewType = 'catCoding';

	private readonly _panel: vscode.WebviewPanel;
	private readonly _extensionPath: string;
    private _disposables: vscode.Disposable[] = [];

	private clientId: string;
	private processId: string;

	public static createOrShow(extensionPath: string, client: LanguageClient, clientId: string, processId: string) {
		const column = vscode.window.activeTextEditor
			? vscode.window.activeTextEditor.viewColumn
			: undefined;

		// If we already have a panel, show it.
		if (LiveBeansView.currentPanels.has(clientId)) {
			LiveBeansView.currentPanels.get(clientId)._panel.reveal(column);
			return;
		}

		// Otherwise, create a new panel.
		const panel = vscode.window.createWebviewPanel(
			LiveBeansView.viewType,
			clientId,
			column || vscode.ViewColumn.One,
			{
				// Enable javascript in the webview
                enableScripts: true,
				
				// And restrict the webview to only loading content from our extension's `media` directory.
				localResourceRoots: [vscode.Uri.file(path.join(extensionPath, 'media'))]
			}
		);

        LiveBeansView.currentPanels.set(clientId, new LiveBeansView(panel, extensionPath, clientId, processId));

		console.log('Created webview panel!');
		const bridge: LSWebViewToLSPBridge = new LSWebViewToLSPBridge(panel, client);
        panel.webview.onDidReceiveMessage(message => bridge.sendToLs(message));
	}

	// public static revive(panel: vscode.WebviewPanel, extensionPath: string) {
	// 	LiveBeansView.currentPanels.set() = new LiveBeansView(panel, extensionPath);
	// }

	private constructor(panel: vscode.WebviewPanel, extensionPath: string, clientId: string, processId: string) {
		this.clientId = clientId;
		this.processId = processId;
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

	public dispose() {
		LiveBeansView.currentPanels.delete(this.clientId);

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
		this._panel.webview.html = this._getHtmlForWebview();
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

	private _getHtmlForWebview() {
        
		// And the uri we use to load this script in the webview
		const scriptUri = this.mediaUrl('bundle.js');
        const cssUri = this.mediaUrl('css', 'page.css');
        
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
                <div class="row" id="sprotty-app" client-id="${this.clientId}" target="${this.processId}">
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
                        <div id="${this.clientId}" class="sprotty"/>
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

	constructor(panel: vscode.WebviewPanel) {
		this.ws = new SockJS('http://localhost:8080/websocket');
		this.ws.addEventListener('message', (event) => {
			console.dir(event.data);
			const actionMessage = JSON.parse(event.data);
			panel.webview.postMessage(actionMessage);
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

	static bridges: LSWebViewToLSPBridge[] = [];
	static initialized = false;

	private client: LanguageClient;
	private panel: vscode.WebviewPanel;

    constructor(panel: vscode.WebviewPanel, client: LanguageClient) {
		this.client = client;
		this.panel = panel;
		if (!LSWebViewToLSPBridge.initialized) {
			LSWebViewToLSPBridge.initialized = true;
			client.onReady().then(() => {
				client.onNotification(SPROTTY_LSP_NOTIFICATION, async (params: any) => {
					LSWebViewToLSPBridge.bridges.forEach(bridge => {
						bridge.panel.webview.postMessage(params)
					});
				});
			});
		}

		LSWebViewToLSPBridge.bridges.push(this);

		panel.onDidDispose(() => {
			const index = LSWebViewToLSPBridge.bridges.indexOf(this);
			if (index >= 0) {
				LSWebViewToLSPBridge.bridges.splice(index, 1);
			}
		});

	}
	
    sendToLs(message: any) {
    	this.client.onReady().then(() => {
    		this.client.sendNotification(SPROTTY_LSP_NOTIFICATION, message);
		});
    }
}
