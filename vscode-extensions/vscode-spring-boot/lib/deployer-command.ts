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
const APP_JAR = 'bootapp.jar';
const terminal = vscode.window.createTerminal({
    name: DEPLOYER_TERMINAL
});


export function subscribeDeployerCommands(context: vscode.ExtensionContext) {
    terminal.show();
    context.subscriptions.push(vscode.commands.registerCommand('springboot.kubernetes-deploy', () => {
        deployToKubernetes(context);
    }));
    context.subscriptions.push(vscode.commands.registerCommand('springboot.pks-getcredentials', () => {
        connectPks();
    }));
    context.subscriptions.push(vscode.commands.registerCommand('springboot.kubernetes-update', () => {
        // updateApp();
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
        stdResult => {
            let results = stdResult.stdout.split('\n');
            results = results.filter((l) => l.length > 0 && !l.startsWith("Name") && !l.startsWith('\n'));
            if (results.length > 0) {
                return vscode.window.showQuickPick(results, { placeHolder: `Please select a PKS cluster:` });
            } else {
                throw new Error('No PKS clusters to select. Use `pks` cli to connect, and verify at least one cluster exists');
            }
    }).then(cluster => {
            let clusterLineVals = cluster.match(/\S+/g) || [];
            let clusterName = clusterLineVals.shift() ;  
            return clusterName;          
    }).then(clusterName => {
        run('get-credentials ' + clusterName);
    }).then(stdResult => {
        // Show cluster info
        terminal.show();
        terminal.sendText('kubectl cluster-info');
    });
};

interface DeploymentConfiguration {
    appName: string,
    image: string,
    replicas: number,
    useNodePort: boolean,
    jarPath: string
}

function deployToKubernetes(context: vscode.ExtensionContext) {
    let projectRoot = vscode.workspace.rootPath;
    if (!projectRoot) {
       throw new Error("No Spring Boot project available to deploy.")
    } 
    let deploymentConfiguration : DeploymentConfiguration = {
        appName: null,
        image: null,
        replicas: 1,
        useNodePort: true,
        jarPath: null
    }

    vscode.window.showInputBox({
        prompt: 'Enter application deployment name: '
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
            prompt: 'Use NodePort: ',
            value: 'true'
        });
    }).then(useNodePort => {
        if (useNodePort === `true`) {
            deploymentConfiguration.useNodePort = true;
        } else {
            deploymentConfiguration.useNodePort = false;
        }
        return vscode.window.showInputBox( {
            prompt: 'Number of replicas: ',
            value: '1'
        });
    }).then(replicas => {
        deploymentConfiguration.replicas = +replicas;
        return deploymentConfiguration;
    }).then(config => {
        deployInTerminal(context, config, projectRoot);
    });
}

function updateApp(context: vscode.ExtensionContext) {
    let projectRoot = vscode.workspace.rootPath;
    if (!projectRoot) {
       throw new Error("No Spring Boot project available to deploy.")
    } 
    let deploymentConfiguration : DeploymentConfiguration = {
        appName: null,
        image: null,
        replicas: 1,
        useNodePort: true,
        jarPath: null
    }

    getDockerImages().then(images => {
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
        return deploymentConfiguration;
    }).then(config => {
        deployInTerminal(context, config, projectRoot);
    });
}

function updateInTerminal(context: vscode.ExtensionContext, config: DeploymentConfiguration, projectRoot: string) {
    terminal.show();

    // Build the spring boot app to be deployed
    terminal.sendText('mvn clean package -DskipTests');

    // Rename the built app jar to something that can be passed to the deployer
    const source = projectRoot + '/target/*.jar';
    const target = projectRoot + '/target/' + APP_JAR;
    terminal.sendText('cp ' + source + ' ' + target);
    config.jarPath = target;
    
    let deployerJar = findDeployerJar(Path.resolve(context.extensionPath, 'jars'));
  
    terminal.sendText('java -jar ' + deployerJar + ' ' + getDeployArgs(config));
}

function deployInTerminal(context: vscode.ExtensionContext, config: DeploymentConfiguration, projectRoot: string) {
    terminal.show();
    terminal.sendText('echo ' + config);

    // Build the spring boot app to be deployed
    terminal.sendText('mvn clean package -DskipTests');

    // Rename the built app jar to something that can be passed to the deployer
    const source = projectRoot + '/target/*.jar';
    const target = projectRoot + '/target/' + APP_JAR;
    terminal.sendText('cp ' + source + ' ' + target);
    config.jarPath = target;
    
    let deployerJar = findDeployerJar(Path.resolve(context.extensionPath, 'jars'));
  
    terminal.sendText('java -jar ' + deployerJar + ' ' + getDeployArgs(config));
}

function getDeployArgs(deploymentConfiguration: DeploymentConfiguration): string {
    return 'name:' 
    + deploymentConfiguration.appName 
    + ' image:' 
    + deploymentConfiguration.image 
    + ' use-node-port:'
    + deploymentConfiguration.useNodePort
    + ' replicas:' 
    + deploymentConfiguration.replicas 
    + ' deploy '
    + 'jarPath:'
    + deploymentConfiguration.jarPath;
}   

function getUpdateArgs(deploymentConfiguration: DeploymentConfiguration): string {
    return  'image:' 
    + deploymentConfiguration.image 
    + ' update '
    + 'jarPath:'
    + deploymentConfiguration.jarPath;
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

