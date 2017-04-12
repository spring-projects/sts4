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
import { LanguageClient, LanguageClientOptions, SettingMonitor, ServerOptions, StreamInfo } from 'vscode-languageclient';
import { TextDocument, OutputChannel, Disposable, window } from 'vscode';
import { Trace } from 'vscode-jsonrpc';

PortFinder.basePort = 45556;

const DEBUG_ARG = '-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y';

export interface ActivatorOptions {
    DEBUG: boolean;
    CONNECT_TO_LS?: boolean;
    TRACE?: boolean;
    extensionId: string;
    clientOptions: LanguageClientOptions;
    fatJarFile: string;
    jvmHeap?: string;
}

export function activate(options: ActivatorOptions, context: VSCode.ExtensionContext): Promise<LanguageClient> {
    let DEBUG = options.DEBUG;
    let jvmHeap = options.jvmHeap;
    if (options.CONNECT_TO_LS) {
        return connectToLS(context, options);
    } else {
        let clientOptions = options.clientOptions;
        let fatJarFile = Path.resolve(context.extensionPath, options.fatJarFile);

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

        let javaExecutablePath = findJavaExecutable('java');

        if (javaExecutablePath == null) {
            VSCode.window.showErrorMessage("Couldn't locate java in $JAVA_HOME or $PATH");
            return;
        }
        log("Found java exe: " + javaExecutablePath);


        return isJava8(javaExecutablePath).then(eight => {
            if (!eight) {
                VSCode.window.showErrorMessage('Java-based Language Server requires Java 8 (using ' + javaExecutablePath + ')');
                return;
            }
            log("isJavaEight => true");

            function createServer(): Promise<StreamInfo> {
                return new Promise((resolve, reject) => {
                    PortFinder.getPort((err, port) => {
                        Net.createServer(socket => {
                            log('Child process connected on port ' + port);

                            resolve({
                                reader: socket,
                                writer: socket
                            });
                        }).listen(port, () => {
                            let options = {
                                cwd: VSCode.workspace.rootPath
                            };
                            let child: ChildProcess.ChildProcess;
                            let args = [
                                '-Dserver.port=' + port,
                                '-jar',
                                fatJarFile,
                            ];
                            if (jvmHeap) {
                                args.unshift("-Xmx"+jvmHeap);
                            }
                            if (DEBUG) {
                                args.unshift(DEBUG_ARG);
                            }
                            log("CMD = " + javaExecutablePath + ' ' + args.join(' '));

                            // Start the child java process
                            child = ChildProcess.execFile(javaExecutablePath, args, options);
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

            return Promise.resolve(setupLanguageClient(context, createServer, options));
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

    return Promise.resolve(setupLanguageClient(context, serverOptions, options));
}

function setupLanguageClient(context: VSCode.ExtensionContext, createServer: ServerOptions, options: ActivatorOptions): LanguageClient {
    // Create the language client and start the client.
    let client = new LanguageClient(options.extensionId, options.extensionId,
        createServer, options.clientOptions
    );
    if (options.TRACE) {
        client.trace = Trace.Verbose;
    }

    let progressService = new ProgressService();
    client.onNotification({ method: "sts/progress" }, (params: ProgressParams) => {
        progressService.handle(params);
    });
    let disposable = client.start();

    // Push the disposable to the context's subscriptions so that the 
    // client can be deactivated on extension deactivation
    context.subscriptions.push(disposable);
    context.subscriptions.push(progressService);
    return client
}


function isJava8(javaExecutablePath: string): Promise<boolean> {
    return new Promise((resolve, reject) => {
        let result = ChildProcess.execFile(javaExecutablePath, ['-version'], {}, (error, stdout, stderr) => {
            let eight = stderr.indexOf('1.8') >= 0;
            resolve(eight);
        });
    });
}

function findJavaExecutable(binname: string) {
    binname = correctBinname(binname);

    // First search each JAVA_HOME bin folder
    if (process.env['JAVA_HOME']) {
        let workspaces = process.env['JAVA_HOME'].split(Path.delimiter);
        for (let i = 0; i < workspaces.length; i++) {
            let binpath = Path.join(workspaces[i], 'bin', binname);
            if (FS.existsSync(binpath)) {
                return binpath;
            }
        }
    }

    // Then search PATH parts
    if (process.env['PATH']) {
        let pathparts = process.env['PATH'].split(Path.delimiter);
        for (let i = 0; i < pathparts.length; i++) {
            let binpath = Path.join(pathparts[i], binname);
            if (FS.existsSync(binpath)) {
                return binpath;
            }
        }
    }

    // Else return the binary name directly (this will likely always fail downstream) 
    return null;
}

function correctBinname(binname: string) {
    if (process.platform === 'win32')
        return binname + '.exe';
    else
        return binname;
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
