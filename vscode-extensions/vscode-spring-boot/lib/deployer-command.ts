'use strict';

import {hasResults, run, StdResult} from './os-util';
import * as vscode from 'vscode';
import * as Path from 'path';
import * as FS from 'fs';

// NOTE: Be sure to add this under "contributes" in package.json to enable the command:
//
// "commands": [
//     {
//       "command": "springboot.kubernetes-deployer",
//       "title": "Spring Boot: Kubernetes - Deploy Application"
//     }
//   ],
//

const DEPLOYER_TERMINAL : string = 'Spring Boot Deployer';
const DEPLOYER_APP_JAR_NAME: string = 'kubernetes-deployer';


export function subscribeDeployerCommand(context: vscode.ExtensionContext) {
    context.subscriptions.push(vscode.commands.registerCommand('springboot.kubernetes-deployer', () => {
        deployToPks(context);
    }));
}

let Docker =  require('dockerode');
var docker = new Docker();

async function getDockerImages() : Promise<string[]> {
    let images = await docker.listImages();
    let imageVals : string[] = [];
    images.forEach(element => {
        let tags = element.RepoTags;
        if (tags) {
             tags.forEach(tag => {
                 imageVals.push(tag);
             });
        } 
     });

    return imageVals;
}

function runPks(command: string) : Promise<StdResult> {
    let cmd = pksCli() + ' ' + command;
    return run(cmd);
}

function pksCli(): string {
    return 'pks';
}

export function connectPks() : Promise<any>  {
    return runPks('clusters').then(
        standardResult => {
            let results = standardResult.stdout.split('\n');
            results = results.filter((l) => l.length > 0 && !l.startsWith("Name") && !l.startsWith('\n'));
            return vscode.window.showQuickPick(results, { placeHolder: `Please select a cluster:` });
    }).then(cluster => {
            let clusterLineVals = cluster.match(/\S+/g) || [];
            let clusterName = clusterLineVals.shift() ;  
            return clusterName;          
    }).then(clusterName => {
        run('get-credentials ' + clusterName);
    });
};

interface DeploymentConfiguration {
    appName: string,
    image: string,
    replicas: number
}

function deployToPks(context: vscode.ExtensionContext) {
    let projectRoot = vscode.workspace.rootPath;
    if (!projectRoot) {
       throw new Error("No Spring Boot project available to deploy.")
    } 
    let deploymentConfiguration : DeploymentConfiguration = {
        appName: null,
        image: null,
        replicas: 1
    }

    connectPks().then(stdResult => {
        return vscode.window.showInputBox({
            prompt: 'Enter application deployment name: '
        });
    }).then(value => {
        deploymentConfiguration.appName = value;
        return getDockerImages();
    }).then(images => {
        let imageQuickPicks = images.map(img => { 
            let item : vscode.QuickPickItem = { 
                label: img, 
                description: img
            };
            return item;
        });
        return vscode.window.showQuickPick(imageQuickPicks, {placeHolder: 'Select an image:' });
    }).then(image => {
        deploymentConfiguration.image = image.label;
        return vscode.window.showInputBox( {
            prompt: 'Number of replicas: '
        });
    }).then(replicas => {
        deploymentConfiguration.replicas = +replicas;
        return deploymentConfiguration;
    }).then(config => {
        const options = {
            name: DEPLOYER_TERMINAL
        };
        const terminal = vscode.window.createTerminal(options);
        terminal.sendText('echo ' + config);
        terminal.show();

        // Show cluster info
        terminal.sendText('kubectl cluster-info');

        // Build the spring boot app to be deployed
        terminal.sendText('mvn clean package -DskipTests');
        terminal.sendText('cp /target/*.jar bootapp.jar');

        let deployerJar = findDeployerJar(Path.resolve(context.extensionPath, 'jars'));

        terminal.sendText('java -jar ' + deployerJar + ' ' + getJarLauncherArgs(config));

    });
}

function getJarLauncherArgs(deploymentConfiguration: DeploymentConfiguration): string {
    return 'name:' 
    + deploymentConfiguration.appName 
    + ' image:' 
    + deploymentConfiguration.image 
    + ' use-node-port:true replicas: ' 
    + deploymentConfiguration.replicas 
    + ' deploy '
    + 'jarPath: bootapp.jar';
}                           

function findDeployerJar(jarsDir) : string {
    let deployerJar = FS.readdirSync(jarsDir).filter(jar => 
        jar.indexOf(DEPLOYER_APP_JAR_NAME)>=0 &&
        jar.endsWith(".jar")
    );
    if (deployerJar.length==0) {
        throw new Error("Server jar not found in "+jarsDir);
    }
    if (deployerJar.length>1) {
        throw new Error("Multiple server jars found in "+jarsDir);
    }
    return Path.resolve(jarsDir, deployerJar[0]);
}

