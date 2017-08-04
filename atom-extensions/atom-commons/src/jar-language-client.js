const cp = require('child_process');
const fs = require('fs');
const path = require('path');
const url = require('url');
const remote = require('remote-file-size');
const PortFinder = require('portfinder');
const net = require('net');
const rpc = require('vscode-jsonrpc');
const {AutoLanguageClient, DownloadFile} = require('atom-languageclient');

export class JarLanguageClient extends AutoLanguageClient {

    constructor(serverDownloadUrl, serverHome) {
        super();

        this.serverHome = serverHome;
        this.serverDownloadUrl = serverDownloadUrl;
        this.serverLauncherJar = path.basename(url.parse(this.serverDownloadUrl).pathname);
    }

    startServerProcess () {
        // //TODO: Remove when debugging is over
        atom.config.set('core.debugLSP', true);

        let childProcess;

        return new Promise((resolve, reject) => {
            PortFinder.getPort((err, port) => {
                let server = net.createServer(socket => {
                    server.close();
                    this.socket = socket;
                    resolve(childProcess);
                });

                server.listen(port, 'localhost', () => {
                    this.launchProcess(port).then(p => childProcess = p);
                });

            });
        });
    }

    launchProcess(port) {
        const command = this.findJavaExecutable('java');

        return this.compatibleJavaVersion(command).then(version => {
            if (version) {
                var args = this.launchVmArgs(version);
                if (version >= 9) {
                    args.push('--add-modules=java.se.ee');
                }
                return this.getOrInstallLauncher().then(launcher => this.doLaunchProcess(command, launcher, port, args));
            } else {
                this.logger.error('Java executable is not Java 8 or higher');
            }
        });
    }

    launchVmArgs(version) {
        return [];
    }

    doLaunchProcess(javaExecutable, launcher, port, args=[]) {
        let vmArgs = args.concat([
            `-Dserver.port=${port}`,
            // Atom doesn't have lazy completion proposals support - completionItem/resolve message. Disable lazy completions
            '-Dlsp.lazy.completions.disable=true',
            '-Dlsp.completions.indentation.enable=true',
            '-jar',
            launcher
        ]);
        this.logger.debug(`starting "${javaExecutable} ${vmArgs.join(' ')}"`);
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

    findJavaExecutable(binname) {
        binname = this.correctBinname(binname);

        // First search each JAVA_HOME bin folder
        if (process.env['JAVA_HOME']) {
            let workspaces = process.env['JAVA_HOME'].split(path.delimiter);
            for (let i = 0; i < workspaces.length; i++) {
                let binpath = path.join(workspaces[i], 'bin', binname);
                if (fs.existsSync(binpath)) {
                    return binpath;
                }
            }
        }

        // Then search PATH parts
        if (process.env['PATH']) {
            let pathparts = process.env['PATH'].split(path.delimiter);
            for (let i = 0; i < pathparts.length; i++) {
                let binpath = path.join(pathparts[i], binname);
                if (fs.existsSync(binpath)) {
                    return binpath;
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

    compatibleJavaVersion(javaExecutablePath) {
        return new Promise((resolve, reject) => {
            cp.execFile(javaExecutablePath, ['-version'], {}, (error, stdout, stderr) => {
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
        });
    }

}

