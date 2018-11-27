import * as path from 'path';
import {getPort} from 'portfinder';
import {Server, createServer, connect} from 'net';
import {AtomEnvironment, Disposable} from 'atom';
import {HighlightParams, ProgressParams, CursorMovementParams, StsAdapter} from './sts-adapter';
import {ActiveServer, LanguageServerProcess} from 'atom-languageclient';
import {AutoLanguageClient} from 'atom-languageclient';
import {findJdk, findJvm, JVM} from '@pivotal-tools/jvm-launch-utils';
import {Writable} from 'stream';

export class JavaProcessLanguageClient extends AutoLanguageClient {

    DEBUG = false;

    private server: Server;

    constructor(protected serverHome: string, protected serverLauncherJar: string) {
        super();
    }

    getServerJar(): string {
        return path.resolve(this.serverHome, this.serverLauncherJar);
    }

    showErrorMessage(detail: string, desc?: string): Promise<any> {
        const atomEnv: AtomEnvironment = atom;
        const notification = atomEnv.notifications.addError('Cannot start Language Server', {
            dismissable: true,
            detail: detail,
            description: desc,
            buttons: [{
                text: 'OK',
                onDidClick: () => {
                    notification.dismiss()
                },
            }]
        });
        return Promise.reject(new Error(detail));
    }

    protected startServerProcess(projectPath: string): LanguageServerProcess | Promise<LanguageServerProcess> {
        // TODO: Remove when debugging is over
        const atomEnv: AtomEnvironment = atom;
        atomEnv.config.set('core.debugLSP', true);

        let childProcess: LanguageServerProcess;

        if (this.DEBUG) {
            return this.connectToLS();
        }

        return new Promise((resolve, reject) => {
            let basePort = Math.floor(Math.random() * 10000) + 40000;
            getPort({port: basePort}, (err, port) => {
                this.server = createServer(socket => {
                    this.socket = socket;
                    resolve(childProcess);
                });

                this.server.listen(port, 'localhost', () => {
                    this.launchProcess(port).then(p => childProcess = p);
                });
            });
        });
    }

    private connectToLS(): LanguageServerProcess | Promise<LanguageServerProcess> {
        return new Promise(resolve => {
            this.socket = connect({
                port: 5007
            });
            const stdout: any = {
                setEncoding: () => {
                    return stdout;
                },
                on: () => stdout
            };
            const process: LanguageServerProcess = {
                stdin: <Writable>{},
                stdout: stdout,
                stderr: stdout,
                pid: -1,
                kill: () => {
                    console.log('fake shutdown');
                },
                on: () => process,
                addListener: () => process,
                prependListener: () => process,
                once: () => process,
                prependOnceListener: () => process,
                removeListener: () => process,
                removeAllListeners: () => process,
                setMaxListeners: () => process,
                getMaxListeners: () => 0,
                listeners: () => [],
                emit: () => false,
                eventNames: () => [],
                listenerCount: () => 0
            };
            resolve(process);
        });
    }

    // Start adapters that are not shared between servers
    protected postInitialization(server: ActiveServer): void {
        const stsAdapter = this.createStsAdapter() || new StsAdapter();
        (<any>server.connection)._onRequest({method: 'sts/moveCursor'}, (params: CursorMovementParams) => stsAdapter.onMoveCursor(params));
        server.connection.onCustom('sts/progress', (params: ProgressParams) => stsAdapter.onProgress(params));
        server.connection.onCustom('sts/highlight', (params: HighlightParams) => stsAdapter.onHighlight(params));

        server.disposable.add(new Disposable(() => {
            if (this.server) {
                this.server.close()
            }
        }));

    }

    preferJdk(): boolean {
        return false;
    }

    findJvm(): Promise<JVM | null> {
        return this.preferJdk() ? findJdk() : findJvm();
    }

    private launchProcess(port: number): Promise<LanguageServerProcess> {
        return this.findJvm()
            .catch(error => {
                return this.showErrorMessage("Error trying to find JVM", ""+error);
            })
            .then(jvm => {
                if (!jvm) {
                    return this.showErrorMessage("Couldn't locate java in $JAVA_HOME or $PATH");
                }
                let version = jvm.getMajorVersion();
                if (version<8) {
                    return this.showErrorMessage(
                        'No compatible Java Runtime Environment found',
                        'The Java Runtime Environment is either below version "1.8" or is missing from the system'
                    );
                }
                return this.launchVmArgs(jvm).then(args => {
                    args.push(`-Dserver.port=${port}`);
                    return this.doLaunchProcess(
                        jvm,
                        this.getServerJar(),
                        args
                    );
                });
            });
    }

    protected launchVmArgs(jvm: JVM): Promise<string[]> {
        return Promise.resolve([]);
    }

    private doLaunchProcess(jvm: JVM, launcher: string, args: string[] =[]): LanguageServerProcess {
        let vmArgs = args.concat([
            '-Dsts.lsp.client=atom',
            // '-Dlsp.completions.indentation.enable=true', // Looks like Atom has magic indents same like VSCode - comment it out
            '-Dlsp.yaml.completions.errors.disable=true',
        ]);

        this.logger.debug(`starting "${jvm.getJavaExecutable()} ${vmArgs.join('\n')}\n-jar ${launcher}"`);
        return jvm.jarLaunch(launcher, vmArgs, { cwd: this.serverHome });
    }

    createStsAdapter(): StsAdapter | null {
        return null;
    }

}

