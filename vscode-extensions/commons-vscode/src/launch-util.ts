'use strict';

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
import { JVM, findJvm, findJdk } from '@pivotal-tools/jvm-launch-utils';
import { registerClasspathService } from './classpath';
import { registerProjectService } from './project';

let p2c = P2C.createConverter();

PortFinder.basePort = 45556;

const DEBUG_ARG = '-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y';

export interface ActivatorOptions {
    DEBUG: boolean;
    CONNECT_TO_LS?: boolean;
    TRACE?: boolean;
    extensionId: string;
    clientOptions: LanguageClientOptions;
    jvmHeap?: string;
    workspaceOptions: VSCode.WorkspaceConfiguration;
    checkjvm?: (context: VSCode.ExtensionContext, jvm: JVM) => any;
    preferJdk?: boolean;
}

type JavaOptions = {
    heap?: string
    home?: string
}

function getUserDefinedJvmHeap(wsOpts : VSCode.WorkspaceConfiguration,  dflt : string) : string {
    if (!wsOpts) {
        return dflt;
    }
    let javaOptions : JavaOptions = wsOpts.get("java");
    return (javaOptions && javaOptions.heap) || dflt;
}

function getUserDefinedJavaHome(wsOpts : VSCode.WorkspaceConfiguration) : string {
    if (!wsOpts) {
        return null;
    }
    let javaOptions : JavaOptions = wsOpts.get("java");
    return javaOptions && javaOptions.home;
}

export function activate(options: ActivatorOptions, context: VSCode.ExtensionContext): Thenable<LanguageClient> {
    let DEBUG = options.DEBUG;
    let jvmHeap = getUserDefinedJvmHeap(options.workspaceOptions, options.jvmHeap);
    if (options.CONNECT_TO_LS) {
        return VSCode.window.showInformationMessage("Start language server")
        .then((x) => connectToLS(context, options));
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

        let findJRE = options.preferJdk ? findJdk : findJvm;

        return findJRE(getUserDefinedJavaHome(options.workspaceOptions))
        .catch(error => {
            VSCode.window.showErrorMessage("Error trying to find JVM: "+error);
            return Promise.reject(error);
        })
        .then((jvm) => {
            if (!jvm) {
                VSCode.window.showErrorMessage("Couldn't locate java in $JAVA_HOME or $PATH");
                return;
            }
            let javaExecutablePath = jvm.getJavaExecutable();
            log("Found java exe: " + javaExecutablePath);
    
            let version = jvm.getMajorVersion();
            if (version<8) {
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
                            let logfile = Path.join(tmpdir(), options.extensionId + '-' + Date.now()+'.log');
                            log('Redirecting server logs to ' + logfile);
                            const args = [
                                '-Dspring.lsp.client-port='+port,
                                '-Dserver.port=' + port,
                                '-Dsts.lsp.client=vscode',
                                '-Dsts.log.file=' + logfile
                            ];
                            if (options.checkjvm) {
                                options.checkjvm(context, jvm);
                            }
                            if (jvmHeap) {
                                args.unshift("-Xmx"+jvmHeap);
                            }
                            if (DEBUG) {
                                args.unshift(DEBUG_ARG);
                            }

                            // Start the child java process
                            let launcher = findServerJar(Path.resolve(context.extensionPath, 'jars'));
                            let child = jvm.jarLaunch(launcher, args, processLaunchoptions);
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

function findServerJar(jarsDir) : string {
    let serverJars = FS.readdirSync(jarsDir).filter(jar => 
        jar.indexOf('language-server')>=0 &&
        jar.endsWith(".jar")
    );
    if (serverJars.length==0) {
        throw new Error("Server jar not found in "+jarsDir);
    }
    if (serverJars.length>1) {
        throw new Error("Multiple server jars found in "+jarsDir);
    }
    return Path.resolve(jarsDir, serverJars[0]);
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
        registerProjectService(client);
        registerClasspathService(client);
        return client;
    });
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
