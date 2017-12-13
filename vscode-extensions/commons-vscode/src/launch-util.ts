import * as code from 'vscode';

'use strict';
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below

import * as VSCode from 'vscode';
import * as Path from 'path';
import * as FS from 'fs';
import PortFinder = require('portfinder');
import * as Net from 'net';
import * as ChildProcess from 'child_process';
import { TextDocumentIdentifier, RequestType, LanguageClient, LanguageClientOptions, SettingMonitor, ServerOptions, StreamInfo, Range } from 'vscode-languageclient';
import { TextDocument, OutputChannel, Disposable, window } from 'vscode';
import { Trace, NotificationType } from 'vscode-jsonrpc';
import * as P2C from 'vscode-languageclient/lib/protocolConverter';
import {WorkspaceEdit, Position} from 'vscode-languageserver-types';
import {HighlightService, HighlightParams} from './highlight-service';
import { log } from 'util';
import { tmpdir } from 'os';

let p2c = P2C.createConverter();

PortFinder.basePort = 45556;

const DEBUG_ARG = '-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y';

export interface ActivatorOptions {
    DEBUG: boolean;
    CONNECT_TO_LS?: boolean;
    TRACE?: boolean;
    extensionId: string;
    clientOptions: LanguageClientOptions;
    launcher: (context: VSCode.ExtensionContext) => string;
    jvmHeap?: string;
    classpath?: (context: VSCode.ExtensionContext) => string[];
}

export function activate(options: ActivatorOptions, context: VSCode.ExtensionContext): Promise<LanguageClient> {
    let DEBUG = options.DEBUG;
    let jvmHeap = options.jvmHeap;
    if (options.CONNECT_TO_LS) {
        return connectToLS(context, options);
    } else {
        let clientOptions = options.clientOptions;

        var log_output = VSCode.window.createOutputChannel(options.extensionId + "-debug-log");
        log("Activating '" + options.extensionId + "' extension");

        function log(msg: string) {
            if (log_output) {
                log_output.append(msg + "\n");
            }
        }

        function error(msg: string) {
            if (log_output) {
                log_output.append("ERR: " + msg + "\n");
            }
        }

        let javaExecutablePath = findJvmFile('bin', correctBinname('java'));

        if (javaExecutablePath == null) {
            VSCode.window.showErrorMessage("Couldn't locate java in $JAVA_HOME or $PATH");
            return;
        }
        log("Found java exe: " + javaExecutablePath);


        return javaVersion(javaExecutablePath).then(version => {
            if (!version) {
                VSCode.window.showErrorMessage('Java-based Language Server requires Java 8 or higher (using ' + javaExecutablePath + ')');
                return;
            }
            log("isJavaEightOrHigher => true");

            function createServer(): Promise<StreamInfo> {
                return new Promise((resolve, reject) => {
                    PortFinder.getPort((err, port) => {
                        Net.createServer(socket => {
                            log('Child process connected on port ' + port);

                            resolve({
                                reader: socket,
                                writer: socket
                            });
                        })
                        .listen(port, () => {
                            let processLaunchoptions = {
                                cwd: VSCode.workspace.rootPath
                            };
                            let child: ChildProcess.ChildProcess;
                            let logfile = Path.join(tmpdir(), options.extensionId + '-' + Date.now()+'.log');
                            log('Redirecting server logs to ' + logfile);
                            const args = [
                                '-Dserver.port=' + port,
                                '-Dorg.slf4j.simpleLogger.logFile=' + logfile
                            ];
                            if (options.classpath) {
                                const classpath = options.classpath(context);
                                if (classpath) {
                                    args.push('-cp');
                                    args.push(classpath.join(Path.delimiter));
                                }
                            }
                            const launcher = options.launcher(context);
                            if (launcher.endsWith('.jar')) {
                                args.push('-jar');
                            }
                            args.push(options.launcher(context));
                            if (jvmHeap) {
                                args.unshift("-Xmx"+jvmHeap);
                            }
                            if (DEBUG) {
                                args.unshift(DEBUG_ARG);
                            }
                            log("CMD = " + javaExecutablePath + ' ' + args.join(' '));

                            // Start the child java process
                            child = ChildProcess.execFile(javaExecutablePath, args, processLaunchoptions);
                            child.stdout.on('data', (data) => {
                                log("" + data);
                            });
                            child.stderr.on('data', (data) => {
                                error("" + data);
                            })
                        });
                    });
                });
            }

            return setupLanguageClient(context, createServer, options);
        });
    }
}

function connectToLS(context: VSCode.ExtensionContext, options: ActivatorOptions): Promise<LanguageClient> {
    let connectionInfo = {
        port: 5007
    };

    let serverOptions = () => {
        let socket = Net.connect(connectionInfo);
        let result: StreamInfo = {
            writer: socket,
            reader: socket
        };
        return Promise.resolve(result);
    };

    return setupLanguageClient(context, serverOptions, options);
}

function setupLanguageClient(context: VSCode.ExtensionContext, createServer: ServerOptions, options: ActivatorOptions): Promise<LanguageClient> {
    // Create the language client and start the client.
    let client = new LanguageClient(options.extensionId, options.extensionId,
        createServer, options.clientOptions
    );
    client.registerProposedFeatures();
    log("Proposed protocol extensions loaded!");
    if (options.TRACE) {
        client.trace = Trace.Verbose;
    }

    let progressNotification = new NotificationType<ProgressParams,void>("sts/progress");
    let highlightNotification = new NotificationType<HighlightParams,void>("sts/highlight");
    let moveCursorRequest = new RequestType<MoveCursorParams,MoveCursorResponse,void,void>("sts/moveCursor");

    let disposable = client.start();

    let progressService = new ProgressService();
    let highlightService = new HighlightService();
    context.subscriptions.push(disposable);
    context.subscriptions.push(progressService);
    context.subscriptions.push(highlightService);
    return  client.onReady().then(() => {
        client.onNotification(progressNotification, (params: ProgressParams) => {
            progressService.handle(params);
        });
        client.onNotification(highlightNotification, (params: HighlightParams) => {
            highlightService.handle(params);
        });
        client.onRequest(moveCursorRequest, (params: MoveCursorParams) => {
            let editors = VSCode.window.visibleTextEditors;
            for (let editor of editors) {
                if (editor.document.uri.toString() == params.uri) {
                    let cursor = p2c.asPosition(params.position);
                    let selection : VSCode.Selection = new VSCode.Selection(cursor, cursor);
                    editor.selections = [ selection ];
                }
            }
            return { applied: true};
        });
        return client;
    });
}

function javaVersion(javaExecutablePath: string): Promise<number> {
    return new Promise((resolve, reject) => {
        ChildProcess.execFile(javaExecutablePath, ['-version'], {}, (error, stdout, stderr) => {
            if (stderr.indexOf('1.8') >= 0) {
                resolve(8);
            } else if (stderr.indexOf('java version "9') >= 0) {
                resolve(9);
            } else {
                resolve(0);
            }
        });
    });
}

export function findJvmFile(folderPath: string, file: string): string {
    // First search each JAVA_HOME bin folder
    if (process.env['JAVA_HOME']) {
        let workspaces = process.env['JAVA_HOME'].split(Path.delimiter);
        for (let i = 0; i < workspaces.length; i++) {
            let filePath = Path.join(workspaces[i], folderPath, file);
            if (FS.existsSync(filePath)) {
                return filePath;
            }
        }
    }

    // Then search PATH parts
    if (process.env['PATH']) {
        let pathparts = process.env['PATH'].split(Path.delimiter);
        for (let i = 0; i < pathparts.length; i++) {
            let filePath = Path.join(pathparts[i], file);
            if (FS.existsSync(filePath)) {
                return filePath;
            }
        }
    }
}

function correctBinname(binname: string) {
    if (process.platform === 'win32')
        return binname + '.exe';
    else
        return binname;
}

interface MoveCursorParams {
    uri: string
    position: Position
}

interface MoveCursorResponse {
    applied: boolean
}

interface ProgressParams {
    id: string
    statusMsg?: string
}

class ProgressService {

    private status = new Map<String, Disposable>();

    handle(params: ProgressParams) {
        let oldMessage = this.status.get(params.id);
        if (oldMessage) {
            oldMessage.dispose();
        }
        if (params.statusMsg) {
            let newMessage = window.setStatusBarMessage(params.statusMsg);
            this.status.set(params.id, newMessage);
        }
    }

    dispose() {
        if (this.status) {
            for (let d of this.status.values()) {
                d.dispose();
            }
        }
        this.status = null;
    }
}
