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

import {findJdk, findJvm} from '@pivotal-tools/jvm-launch-utils';

export class JavaProcessLanguageClient extends AutoLanguageClient {

    DEBUG = false;

    constructor(serverDownloadUrl, serverHome, serverLauncherJar) {
        super();

        this.serverHome = serverHome;
        this.serverDownloadUrl = serverDownloadUrl;
        this.serverLauncherJar = serverLauncherJar;
    }

    getServerJar() {
        return path.resolve(this.serverHome, this.serverLauncherJar);
    }

    showErrorMessage(detail, desc) {
        const notification = atom.notifications.addError('Cannot start Language Server', {
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

    preferJdk() {
        return false;
    }

    findJvm() {
        return this.preferJdk() ? findJdk() : findJvm();
    }

    launchProcess(port) {
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
                    port, 
                    args
                );
            });
        });
    }

    launchVmArgs(jvm) {
        return Promise.resolve([]);
    }

    doLaunchProcess(jvm, launcher, port, args=[]) {
        let vmArgs = args.concat([
            // Atom doesn't have lazy completion proposals support - completionItem/resolve message. Disable lazy completions
            '-Dlsp.lazy.completions.disable=true',
            '-Dlsp.completions.indentation.enable=true',
            '-Dlsp.yaml.completions.errors.disable=true',
        ]);

        this.logger.debug(`starting "${jvm.getJavaExecutable()} ${vmArgs.join('\n')}\n-jar ${launcher}"`);
        return jvm.jarLaunch(launcher, vmArgs, { cwd: this.serverHome });
    }

    installServer () {
        const localFileName = this.getServerJar();
        this.logger.log(`Downloading ${this.serverDownloadUrl} to ${localFileName}`);
        return this.fileExists(this.serverHome)
                .then(doesExist => { if (!doesExist) fs.mkdir(this.serverHome) })
                .then(() => this.remoteFileSize(this.serverDownloadUrl))
                .then((size) => DownloadFile(this.serverDownloadUrl, localFileName, (bytesDone, percent) => this.handleDownlaodPercentChange(bytesDone, size, percent), size))
                .then(() => this.fileExists(this.getServerJar()))
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

