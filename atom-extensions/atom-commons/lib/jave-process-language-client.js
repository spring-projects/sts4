const cp = require('child_process');
const fs = require('fs');
const path = require('path');
const url = require('url');
const remote = require('remote-file-size');
const PortFinder = require('portfinder');
const net = require('net');
const rpc = require('vscode-jsonrpc');
const {AutoLanguageClient, DownloadFile} = require('atom-languageclient');
const { Disposable } = require('atom');
import { StsAdapter } from './sts-adapter';


export class JavaProcessLanguageClient extends AutoLanguageClient {

    DEBUG = false;

    constructor(serverDownloadUrl, serverHome, serverLauncherJar) {
        super();

        this.serverHome = serverHome;
        this.serverDownloadUrl = serverDownloadUrl;
        this.serverLauncherJar = serverLauncherJar;
    }

    getInitializeParams(projectPath, process) {
        const initParams = super.getInitializeParams(projectPath, process);
        initParams.capabilities = {
            workspace: {
                executeCommand: {
                }
            }
        };
        return initParams;
    }

    startServerProcess () {
        // //TODO: Remove when debugging is over
        atom.config.set('core.debugLSP', true);

        let childProcess;

        if (this.DEBUG) {
            return this.connectToLS();
        }

        return new Promise((resolve, reject) => {
            let basePort = Math.floor(Math.random() * 10000) + 40000;
            PortFinder.getPort({port: basePort}, (err, port) => {
                this.server = net.createServer(socket => {
                    this.socket = socket;
                    resolve(childProcess);
                });

                this.server.listen(port, 'localhost', () => {
                    this.launchProcess(port).then(p => childProcess = p);
                });

            });
        });
    }

    connectToLS() {
        return new Promise(resolve => {
            this.socket = net.connect({
                port: 5007
            });
            resolve({
                pid: -1,
                kill: function() {
                    console.log('fake shutdown');
                }
            })
        });
    }

    // Start adapters that are not shared between servers
    startExclusiveAdapters(server) {
        super.startExclusiveAdapters(server);

        const stsAdapter = this.createStsAdapter() || new StsAdapter();
        server.connection._onRequest({method: 'sts/moveCursor'}, params => stsAdapter.onMoveCursor(params));
        server.connection._onNotification({method: 'sts/progress'}, params => stsAdapter.onProgress(params));
        server.connection._onNotification({method: 'sts/highlight'}, params => stsAdapter.onHighlight(params));
    }


    launchProcess(port) {
        const command = this.findJavaFile('bin', this.correctBinname('java'));

        return this.javaVesrion(command).then(version => {
            if (version) {
                return this.launchVmArgs(version).then(args => {
                    args.push(`-Dserver.port=${port}`);
                    return this.getOrInstallLauncher().then(launcher => this.doLaunchProcess(command, launcher, port, args));
                });
            } else {
                this.logger.error('Java executable is not Java 8 or higher');
                const notification = atom.notifications.addError('Cannot start Language Server', {
                    dismissable: true,
                    detail: 'No compatible Java Runtime Environment found',
                    description: 'The Java Runtime Environment is either below version "1.8" or is missing from the system',
                    buttons: [{
                        text: 'OK',
                        onDidClick: () => {
                            notification.dismiss()
                        },
                    }],
                });
            }
        });
    }

    launchVmArgs(version) {
        return Promise.resolve([]);
    }

    doLaunchProcess(javaExecutable, launcher, port, args=[]) {
        let vmArgs = args.concat([
            // Atom doesn't have lazy completion proposals support - completionItem/resolve message. Disable lazy completions
            '-Dlsp.lazy.completions.disable=true',
            '-Dlsp.completions.indentation.enable=true',
            '-Dlsp.yaml.completions.errors.disable=true',
        ]);
        if (launcher.endsWith('.jar')) {
            vmArgs.push('-jar');
        }
        vmArgs.push(launcher);
        this.logger.debug(`starting "${javaExecutable} ${vmArgs.join('\n')}"`);
        return cp.spawn(javaExecutable, vmArgs, { cwd: this.serverHome })
    }

    getOrInstallLauncher() {
        const fullLauncherJar = path.join(this.serverHome, this.serverLauncherJar);
        return this.fileExists(fullLauncherJar).then(doesExist =>
            doesExist ? fullLauncherJar : this.installServer().then(() => fullLauncherJar)
        );
    }

    installServer () {
        const localFileName = path.join(this.serverHome, this.serverLauncherJar);
        this.logger.log(`Downloading ${this.serverDownloadUrl} to ${localFileName}`);
        return this.fileExists(this.serverHome)
                .then(doesExist => { if (!doesExist) fs.mkdir(this.serverHome) })
                .then(() => this.remoteFileSize(this.serverDownloadUrl))
                .then((size) => DownloadFile(this.serverDownloadUrl, localFileName, (bytesDone, percent) => this.handleDownlaodPercentChange(bytesDone, size, percent), size))
                .then(() => this.fileExists(path.join(this.serverHome, this.serverLauncherJar)))
                .then(doesExist => { if (!doesExist) throw Error(`Failed to install the ${this.getServerName()} language server`) })
                .then(() => this.handleServerInstalled())
                .then(() => Promise.resolve(true));
    }

    handleDownlaodPercentChange(bytesDone, size, percent) {

    }

    handleServerInstalled() {

    }

    preInitialization(connection) {
        connection.onCustom('language/status', (e) => this.updateStatusBar(`${e.type.replace(/^Started$/, '')} ${e.message}`));
    }

    remoteFileSize(url) {
        return new Promise((resolve, reject) => {
            remote(url, (e,s) => {
                if (e) {
                    reject(e);
                } else {
                    resolve(s);
                }
            });
        });
    }

    fileExists (path) {
        return new Promise((resolve, reject) => {
            fs.access(path, fs.R_OK, error => {
                resolve(!error || error.code !== 'ENOENT');
            })
        })
    }

    findJavaFile(folders, file) {

        // First search each JAVA_HOME folder
        if (process.env['JAVA_HOME']) {
            let workspaces = process.env['JAVA_HOME'].split(path.delimiter);
            for (let i = 0; i < workspaces.length; i++) {
                let filepath = path.join(workspaces[i], folders, file);
                if (fs.existsSync(filepath)) {
                    return filepath;
                }
            }
        }

        // Then search PATH parts
        if (process.env['PATH']) {
            let pathparts = process.env['PATH'].split(path.delimiter);
            for (let i = 0; i < pathparts.length; i++) {
                let filepath = path.join(pathparts[i], file);
                if (fs.existsSync(filepath)) {
                    return filepath;
                }
            }
        }

        // Else return the binary name directly (this will likely always fail downstream)
        return null;
    }

    correctBinname(binname) {
        if (process.platform === 'win32')
            return binname + '.exe';
        else
            return binname;
    }

    javaVesrion(javaExecutablePath) {
        return new Promise((resolve, reject) => {
            cp.execFile(javaExecutablePath, ['-version'], {}, (error, stdout, stderr) => {
                if (stderr.indexOf('1.8') >= 0) {
                    resolve(8);
                } else if (stderr.indexOf('java version "9"') >= 0) {
                    resolve(9);
                } else {
                    resolve(0);
                }
            });
        });
    }

    // Late wire-up of listeners after initialize method has been sent
    postInitialization(server) {
        server.disposable.add(new Disposable(() => {
            if (this.server) {
                this.server.close()
            }
        }));
    }

    createStsAdapter() {

    }

}

