'use strict';
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below

import * as VSCode from 'vscode';
import * as Path from 'path';
import * as FS from 'fs';
import * as PortFinder from 'portfinder';
import * as Net from 'net';
import * as ChildProcess from 'child_process';
import {LanguageClient, LanguageClientOptions, SettingMonitor, ServerOptions, StreamInfo} from 'vscode-languageclient';
import {TextDocument} from 'vscode';

PortFinder.basePort = 55282;

var DEBUG = true;
const DEBUG_ARG = '-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n';
    //If DEBUG is falsy then
    //   we launch from the 'fat jar' (which has to be built by running mvn package)
    //if DEBUG is truthy then
    //   - we launch the Java project directly from the classes folder produced by Eclipse JDT compiler
    //   - we add DEBUG_ARG to the launch so that remote debugger can attach on port 8000

function getClasspath(context: VSCode.ExtensionContext):string {
    if (DEBUG) {
        try {
            let projectDir = context.extensionPath;
            let classpathFile = Path.resolve(projectDir, "classpath.txt");
            //TODO: async read?
            let classpath = FS.readFileSync(classpathFile, 'utf8');
            classpath =  Path.resolve(projectDir, 'target/classes') + ':' + classpath;
            return classpath;
        } catch (e) {
            //Expected if you classpath.txt file isn't packaged. So this means we are running in packaged mode.    
            VSCode.window.showInformationMessage('classpath file not found, so disabling DEBUG mode '+e);
            //Nasty! Be careful, this assumes 'getClasspath' is called before computing debug args.
            DEBUG = false;
        }
    }
    return Path.resolve(context.extensionPath, "out", "fat-jar.jar");
}

/** Called when extension is activated */
export function activate(context: VSCode.ExtensionContext) {
    let javaExecutablePath = findJavaExecutable('java');
    
    if (javaExecutablePath == null) {
        VSCode.window.showErrorMessage("Couldn't locate java in $JAVA_HOME or $PATH");
        return;
    }
        
    isJava8(javaExecutablePath).then(eight => {
        if (!eight) {
            VSCode.window.showErrorMessage('Java language support requires Java 8 (using ' + javaExecutablePath + ')');
            return;
        }
                    
        // Options to control the language client
        let clientOptions: LanguageClientOptions = {
            
            // HACK!!! documentSelector only takes string|string[] where string is language id, but DocumentFilter object is passed instead
            // Reasons:
            // 1. documentSelector is just passed over to functions like #registerHoverProvider(documentSelector, ...) that take documentSelector
            // parameter in string | DocumentFilter | string[] | DocumentFilter[] format
            // 2. Combination of non string|string[] documentSelector parameter and synchronize.textDocumentFilter function makes doc synchronization
            // events pass on to Language Server only for documents for which function passed via textDocumentFilter property return true

            // TODO: Remove <any> cast ones https://github.com/Microsoft/vscode-languageserver-node/issues/9 is resolved
            documentSelector: [
                 <any> {language: 'ini', pattern: '**/application*.properties'},
                 <any> {language: 'java-properties', pattern: '**/application*.properties'}
            ],
            synchronize: {
                // Synchronize the setting section to the server:
                configurationSection: 'languageServerExample',
                // Notify the server about file changes to 'javaconfig.json' files contain in the workspace
                fileEvents: [
                    //What's this for? Don't think it does anything useful for this example:
                    VSCode.workspace.createFileSystemWatcher('**/.clientrc')
                ],

                // TODO: Remove textDocumentFilter property ones https://github.com/Microsoft/vscode-languageserver-node/issues/9 is resolved
                textDocumentFilter: function(textDocument : TextDocument) : boolean {
                    return  /^(.*\/)?application[^\s\\/]*.properties$/i.test(textDocument.fileName);
                }
            }
        }

        function createServer(): Promise<StreamInfo> {
            return new Promise((resolve, reject) => {
                PortFinder.getPort((err, port) => {
                    Net.createServer(socket => {
                        console.log('Child process connected on port ' + port);

                        resolve({
                            reader: socket,
                            writer: socket
                        });
                    }).listen(port, () => {
                        let options = { 
                            cwd: VSCode.workspace.rootPath 
                        };
                        let child: ChildProcess.ChildProcess;
                        let classpath = getClasspath(context);
                        let args = [
                            '-Dserver.port=' + port,
                            '-cp', classpath, 
                            'org.springframework.ide.vscode.boot.properties.Main'
                        ];
                        if (DEBUG) {
                            args.unshift(DEBUG_ARG);
                        }
                        console.log(javaExecutablePath + ' ' + args.join(' '));
                        
                        // Start the child java process
                        child = ChildProcess.execFile(javaExecutablePath, args, options);
                        child.stdout.on('data', (data) => {
                            console.log(data);
                        });
                        child.stderr.on('data', (data) => {
                            console.error(data);
                        })
                    });
                });
            });
        }

        // Create the language client and start the client.
        let client = new LanguageClient('lsapi-example', 'Language Server Example', 
            createServer, clientOptions);
        let disposable = client.start();

        // Push the disposable to the context's subscriptions so that the 
        // client can be deactivated on extension deactivation
        context.subscriptions.push(disposable);
    });
}

function isJava8(javaExecutablePath: string): Promise<boolean> {
    return new Promise((resolve, reject) => {
        let result = ChildProcess.execFile(javaExecutablePath, ['-version'], { }, (error, stdout, stderr) => {
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

// this method is called when your extension is deactivated
export function deactivate() {
}
