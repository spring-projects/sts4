'use strict';

import * as VSCode from 'vscode';
import * as Path from 'path';
import * as FS from 'fs';
import PortFinder = require('portfinder');
import * as Net from 'net';
import * as ChildProcess from 'child_process';
import * as CommonsCommands from './commands';
import { TextDocumentIdentifier, RequestType, LanguageClient, LanguageClientOptions, SettingMonitor, ServerOptions, StreamInfo, Position } from 'vscode-languageclient';
import {
    Disposable,
    window,
    Event,
    EventEmitter,
    ProgressLocation,
    Progress,
} from 'vscode';
import { Trace, NotificationType } from 'vscode-jsonrpc';
import * as P2C from 'vscode-languageclient/lib/protocolConverter';
import {HighlightService, HighlightParams} from './highlight-service';
import { log } from 'util';
import { JVM, findJvm, findJdk } from '@pivotal-tools/jvm-launch-utils';
import { registerClasspathService } from './classpath';
import {HighlightCodeLensProvider} from "./code-lens-service";
import {registerJavaDataService} from "./java-data";

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
    highlightCodeLensSettingKey?: string;
    explodedLsJarData?: ExplodedLsJarData;
}

export interface ExplodedLsJarData {
    lsLocation: string;
    mainClass: string;
    configFileName?: string;
}

type JavaOptions = {
    heap?: string
    home?: string
    vmargs?: string[]
}

function getUserDefinedJvmHeap(wsOpts : VSCode.WorkspaceConfiguration,  dflt : string) : string {
    if (!wsOpts) {
        return dflt;
    }
    let javaOptions : JavaOptions = wsOpts.get("java");
    return (javaOptions && javaOptions.heap) || dflt;
}

function isCheckingJVM(wsOpts : VSCode.WorkspaceConfiguration): boolean {
    if (!wsOpts) {
        return true;
    }
    return wsOpts.get("checkJVM");
}

function getUserDefinedJvmArgs(wsOpts : VSCode.WorkspaceConfiguration) : string[] {
    const dflt = [];
    if (!wsOpts) {
        return dflt;
    }
    let javaOptions : JavaOptions = wsOpts.get("java");
    return javaOptions && javaOptions.vmargs || dflt;
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
    let jvmArgs = getUserDefinedJvmArgs(options.workspaceOptions);
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
                            let logfile : string = options.workspaceOptions.get("logfile") || "/dev/null";
                            //The logfile = '/dev/null' is handled specifically by the language server process so it works on all OSs.
                            log('Redirecting server logs to ' + logfile);
                            const args = [
                                '-Dspring.lsp.client-port='+port,
                                '-Dserver.port=' + port,
                                '-Dsts.lsp.client=vscode',
                                '-Dsts.log.file=' + logfile,
                                '-XX:TieredStopAtLevel=1'
                            ];
                            if (isCheckingJVM(options.workspaceOptions) && options.checkjvm) {
                                options.checkjvm(context, jvm);
                            }
                            if (jvmHeap && !hasHeapArg(jvmArgs)) {
                                args.unshift("-Xmx"+jvmHeap);
                            }
                            if (jvmArgs) {
                                args.unshift(...jvmArgs);
                            }
                            if (DEBUG) {
                                args.unshift(DEBUG_ARG);
                            }

                            let child: ChildProcess.ChildProcess = null;
                            if (options.explodedLsJarData) {
                                const explodedLsJarData = options.explodedLsJarData;
                                const lsRoot = Path.resolve(context.extensionPath, explodedLsJarData.lsLocation);

                                // Add config file if needed
                                if (explodedLsJarData.configFileName) {
                                    args.push(`-Dspring.config.location=file:${Path.resolve(lsRoot, `BOOT-INF/classes/${explodedLsJarData.configFileName}`)}`);
                                }

                                // Add classpath
                                const classpath: string[] = [];
                                classpath.push(Path.resolve(lsRoot, 'BOOT-INF/classes'));
                                classpath.push(`${Path.resolve(lsRoot, 'BOOT-INF/lib')}${Path.sep}*`);

                                child = jvm.mainClassLaunch(explodedLsJarData.mainClass, classpath, args, processLaunchoptions);
                            } else {
                                // Start the child java process
                                const launcher = findServerJar(Path.resolve(context.extensionPath, 'jars'));
                                child = jvm.jarLaunch(launcher, args, processLaunchoptions);
                            }
                            if (child) {
                                child.stdout.on('data', (data) => {
                                    log("" + data);
                                });
                                child.stderr.on('data', (data) => {
                                    error("" + data);
                                })
                            }
                        });
                    });
                });
            }
            return setupLanguageClient(context, createServer, options);
        });
    }
}

function hasHeapArg(vmargs?: string[]) : boolean {
    if (vmargs) {
        return vmargs.some(a => a.startsWith("-Xmx"));
    }
    return false;
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

    const codeLensListanableSetting = options.highlightCodeLensSettingKey ? new ListenablePreferenceSetting<boolean>(options.highlightCodeLensSettingKey) : undefined;

    let progressService = new ProgressService();
    let highlightService = new HighlightService();
    const codelensService = new HighlightCodeLensProvider();
    let codeLensProviderSubscription: Disposable;

    CommonsCommands.registerCommands(context);

    context.subscriptions.push(disposable);
    context.subscriptions.push(progressService);
    context.subscriptions.push(highlightService);

    function toggleHighlightCodeLens() {
        if (!codeLensProviderSubscription && codeLensListanableSetting.value) {
            codeLensProviderSubscription = VSCode.languages.registerCodeLensProvider(options.clientOptions.documentSelector, codelensService);
            context.subscriptions.push(codeLensProviderSubscription);
        } else if (codeLensProviderSubscription) {
            codeLensProviderSubscription.dispose();
            const idx = context.subscriptions.indexOf(codeLensProviderSubscription);
            if (idx >= 0) {
                context.subscriptions.splice(idx, 1);
            }
            codeLensProviderSubscription = null;
        }
    }

    if (codeLensListanableSetting) {
        toggleHighlightCodeLens();
        codeLensListanableSetting.onDidChangeValue(() => toggleHighlightCodeLens())
    }

    return  client.onReady().then(() => {
        client.onNotification(progressNotification, (params: ProgressParams) => {
            progressService.handle(params);
        });
        client.onNotification(highlightNotification, (params: HighlightParams) => {
            highlightService.handle(params);
            if (codeLensListanableSetting && codeLensListanableSetting.value) {
                codelensService.handle(params);
            }
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
        registerClasspathService(client);
        registerJavaDataService(client);
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
	title: string
    statusMsg?: string
}

class ProgressHandle {
    constructor(
        private progress: Progress<{ message?: string; increment?: number }>,
        private finish: () => void
    ) {}

    updateStatus(message: string, increment: number) {
        this.progress.report({
            message,
            increment
        });
    }

    complete() {
        this.finish();
    }
}

class ProgressService {

    private status = new Map<String, ProgressHandle>();

    handle(params: ProgressParams) {
        const progressHandler = this.status.get(params.id);
        if (progressHandler) {
			if(params.statusMsg) {
				progressHandler.updateStatus(params.statusMsg, -1);
			} else {
				progressHandler.complete();
			}
        } else {
            if (params.statusMsg) {
                window.withProgress({
                    location: ProgressLocation.Notification,
                    title: "",
                    cancellable: false
                }, progress => new Promise(resolve => {
					this.status.set(params.id, new ProgressHandle(progress, resolve));
					progress.report({
						message: params.statusMsg,
						increment: -1
					})
				}));
            }
        }

    }

    dispose() {
        if (this.status) {
            for (let handler of this.status.values()) {
                handler.complete();
            }
        }
        this.status = null;
    }
}

export interface ListenableSetting<T> {
    value: T;
    onDidChangeValue: VSCode.Event<void>
}

export class ListenablePreferenceSetting<T> implements ListenableSetting<T> {

    private _onDidChangeValue = new EventEmitter<void>();

    constructor(private section: string) {
        VSCode.workspace.onDidChangeConfiguration(e => {
           console.log('Settings changed! value = ' + this.value);
           if (e.affectsConfiguration(this.section)) {
               this._onDidChangeValue.fire();
           }
        });
    }

    get value(): T {
        return VSCode.workspace.getConfiguration().get(this.section);
    }

    get onDidChangeValue(): Event<void> {
        return this._onDidChangeValue.event;
    }

}
