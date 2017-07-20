// const cp = require('child_process');
// const fs = require('fs');
const path = require('path');
// const url = require('url');
// const remote = require('remote-file-size');
// const PortFinder = require('portfinder');
// const net = require('net');
// const rpc = require('vscode-jsonrpc');
// const {AutoLanguageClient, DownloadFile} = require('atom-languageclient');
//
// const serverDownloadUrl = 'https://s3-us-west-1.amazonaws.com/s3-test.spring.io/sts4/fatjars/snapshots/manifest-yaml-language-server-0.0.9-201707121637.jar';
// const serverLauncherJar = path.basename(url.parse(serverDownloadUrl).pathname);
//
// class ManifestYamlLanguageClient extends AutoLanguageClient {
//   getGrammarScopes () { return [ 'source.yaml' ] }
//   getLanguageName () { return 'CF-Manifest-YAML' }
//   getServerName () { return 'CF Manifest YAML' }
//
//   constructor() {
//     super();
//     this.statusElement = document.createElement('span')
//     this.statusElement.className = 'inline-block'
//   }
//
//   startServerProcess () {
//     // //TODO: Remove when debugging is over
//     atom.config.set('core.debugLSP', true);
//
//     let childProcess;
//
//     return new Promise((resolve, reject) => {
//
//       PortFinder.getPort((err, port) => {
//
//         let server = net.createServer(socket => {
//           console.log('Socket is present!');
//           server.close();
//           this.socket = socket;
//           resolve(childProcess);
//         });
//
//         server.listen(port, 'localhost', () => {
//           this.launchProcess(port).then(p => childProcess = p);
//         });
//
//       });
//
//     });
//   }
//
//   launchProcess(port) {
//       const serverHome = path.join(__dirname, '..', 'server');
//       const command = this.findJavaExecutable('java');
//
//       return this.compatibleJavaVersion(command).then(version => {
//         if (version) {
//           var args = [];
//           if (version >= 9) {
//             args.push('--add-modules=java.se.ee');
//           }
//           return this.getOrInstallLauncher(serverHome).then(launcher => this.doLaunchProcess(serverHome, command, launcher, port, args));
//         } else {
//           this.logger.error('Java executable is not Java 8 or higher');
//         }
//       });
//   }
//
//   doLaunchProcess(serverHome, javaExecutable, launcher, port, args=[]) {
//     let vmArgs = args.concat([
//         `-Dserver.port=${port}`,
//         '-Xdebug',
//         '-agentlib:jdwp=transport=dt_socket,address=9000,server=y,suspend=n',
//         '-Dorg.slf4j.simpleLogger.logFile=manifest-yaml.log',
//         '-Dorg.slf4j.simpleLogger.defaultLogLevel=trace',
//         '-Djava.util.logging.config.file=logging.properties',
//         '-jar',
//         launcher
//     ]);
//     this.logger.debug(`starting "${javaExecutable} ${vmArgs.join(' ')}"`);
//     return cp.spawn(javaExecutable, vmArgs, { cwd: serverHome })
//   }
//
//   getOrInstallLauncher (serverHome) {
//     const fullLauncherJar = path.join(serverHome, serverLauncherJar);
//     return this.fileExists(fullLauncherJar).then(doesExist =>
//       doesExist ? fullLauncherJar : this.installServer(serverHome).then(() => fullLauncherJar)
//     );
//   }
//
//   installServer (serverHome) {
//     const localFileName = path.join(serverHome, serverLauncherJar);
//     this.logger.log(`Downloading ${serverDownloadUrl} to ${localFileName}`);
//     return this.fileExists(serverHome)
//       .then(doesExist => { if (!doesExist) fs.mkdir(serverHome) })
//       .then(() => this.remoteFileSize(serverDownloadUrl))
//       .then((size) => DownloadFile(serverDownloadUrl, localFileName, (bytesDone, percent) => this.updateStatusBar(`downloading ${percent}%`), size))
//       .then(() => this.fileExists(path.join(serverHome, serverLauncherJar)))
//       .then(doesExist => { if (!doesExist) throw Error(`Failed to install the ${this.getServerName()} language server`) })
//       .then(() => this.updateStatusBar('installed'))
//       // .then(() => fs.unlink(localFileName))
//       .then(() => Promise.resolve(true));
//   }
//
//   // Determine whether we should start a server for a given editor if we don't have one yet
//   shouldStartForEditor(editor) {
//     return super.shouldStartForEditor(editor) && /.*manifest.*\.yml/.test(editor.getFileName());
//   }
//
//
//   preInitialization(connection) {
//     connection.onCustom('language/status', (e) => this.updateStatusBar(`${e.type.replace(/^Started$/, '')} ${e.message}`));
//   }
//
//   updateStatusBar (text) {
//     this.statusElement.textContent = `${this.name} ${text}`;
//   }
//
//   remoteFileSize(url) {
//     return new Promise((resolve, reject) => {
//       remote(url, (e,s) => {
//         if (e) {
//           reject(e);
//         } else {
//           resolve(s);
//         }
//       });
//     });
//   }
//
//   consumeStatusBar (statusBar) {
//     this.statusTile = statusBar.addRightTile({ item: this.statusElement, priority: 1000 });
//   }
//
//   fileExists (path) {
//     return new Promise((resolve, reject) => {
//       fs.access(path, fs.R_OK, error => {
//         resolve(!error || error.code !== 'ENOENT');
//       })
//     })
//   }
//
//   createServerConnection () {
//     return rpc.createMessageConnection(
//       new rpc.SocketMessageReader(this.socket),
//       new rpc.SocketMessageWriter(this.socket)
//     )
//   }
//
//   createRpcConnection(process) {
//     let connection = super.createRpcConnection(process);
//     connection.trace(rpc.Trace.Messages, console);
//     return connection;
//   }
//
//   findJavaExecutable(binname) {
//     binname = this.correctBinname(binname);
//
//     // First search each JAVA_HOME bin folder
//     if (process.env['JAVA_HOME']) {
//         let workspaces = process.env['JAVA_HOME'].split(Path.delimiter);
//         for (let i = 0; i < workspaces.length; i++) {
//             let binpath = Path.join(workspaces[i], 'bin', binname);
//             if (FS.existsSync(binpath)) {
//                 return binpath;
//             }
//         }
//     }
//
//     // Then search PATH parts
//     if (process.env['PATH']) {
//         let pathparts = process.env['PATH'].split(path.delimiter);
//         for (let i = 0; i < pathparts.length; i++) {
//             let binpath = path.join(pathparts[i], binname);
//             if (fs.existsSync(binpath)) {
//                 return binpath;
//             }
//         }
//     }
//
//     // Else return the binary name directly (this will likely always fail downstream)
//     return null;
//
//     // return '/Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/bin/java';
//
//     // return '/Library/Java/JavaVirtualMachines/jdk-9.jdk/Contents/Home/bin/java';
//   }
//
//   correctBinname(binname) {
//     if (process.platform === 'win32')
//         return binname + '.exe';
//     else
//         return binname;
//   }
//
//   compatibleJavaVersion(javaExecutablePath) {
//     return new Promise((resolve, reject) => {
//       cp.execFile(javaExecutablePath, ['-version'], {}, (error, stdout, stderr) => {
//         cp.execFile(javaExecutablePath, ['-version'], {}, (error, stdout, stderr) => {
//           if (stderr.indexOf('1.8') >= 0) {
//             resolve(8);
//           } else if (stderr.indexOf('java version "9"') >= 0) {
//             resolve(9);
//           } else {
//             resolve(0);
//           }
//         });
//       });
//     });
//   }
//
// }
//
// module.exports = new ManifestYamlLanguageClient();

const { JarLanguageClient } = require('atom-commons');

class ManifestYamlLanguageClient extends JarLanguageClient {

    constructor() {
        super(
            'https://s3-us-west-1.amazonaws.com/s3-test.spring.io/sts4/fatjars/snapshots/manifest-yaml-language-server-0.0.9-201707201812.jar',
            path.join(__dirname, '..', 'server')
        );

        this.statusElement = document.createElement('span');
        this.statusElement.className = 'inline-block';
    }

    getGrammarScopes() {
        return ['source.yaml']
    }

    getLanguageName() {
        return 'CF-Manifest-YAML'
    }

    getServerName() {
        return 'CF Manifest YAML'
    }

    handleDownlaodPercentChange(bytesDone, size, percent) {
        this.updateStatusBar(`downloading ${percent}%`);
    }

    handleServerInstalled() {
        this.updateStatusBar('installed');
    }

    // Determine whether we should start a server for a given editor if we don't have one yet
    shouldStartForEditor(editor) {
        return super.shouldStartForEditor(editor) && /.*manifest.*\.yml/.test(editor.getFileName());
    }

    consumeStatusBar (statusBar) {
        this.statusTile = statusBar.addRightTile({ item: this.statusElement, priority: 1000 });
    }

    updateStatusBar (text) {
        this.statusElement.textContent = `${this.name} ${text}`;
    }

    launchVmArgs(version) {
        return [
            '-Xdebug',
            '-agentlib:jdwp=transport=dt_socket,address=9000,server=y,suspend=n',
            '-Dorg.slf4j.simpleLogger.logFile=manifest-yaml.log',
            '-Dorg.slf4j.simpleLogger.defaultLogLevel=debug',
        ];

    }

}

module.exports = new ManifestYamlLanguageClient();
