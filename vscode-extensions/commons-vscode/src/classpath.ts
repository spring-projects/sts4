'use strict';


import * as VSCode from 'vscode';
import { RequestType } from 'vscode-languageclient';
import { LanguageClient } from 'vscode-languageclient/node';
import { workspace } from 'vscode';

export function registerClasspathService(client : LanguageClient) : void {

    let addRequest = new RequestType<ClasspathListenerParams, ClasspathListenerResponse, void>("sts/addClasspathListener");
    client.onRequest(addRequest, async (params: ClasspathListenerParams) => {
        const jdtConfig = workspace.getConfiguration('java')
        const launchMode = jdtConfig?.get('server.launchMode');
        if (launchMode === 'LightWeight') {
            throw new Error('Classpath listener not supported while Java Language Server is in LightWeight mode');
        }
        return <ClasspathListenerResponse> await VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.addClasspathListener", params.callbackCommandId);
    });

    let removeRequest = new RequestType<ClasspathListenerParams, ClasspathListenerResponse, void>("sts/removeClasspathListener");
    client.onRequest(removeRequest, async (params: ClasspathListenerParams) => {
        return <ClasspathListenerResponse> await VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.removeClasspathListener", params.callbackCommandId);
    });

}

interface ClasspathListenerParams {
    callbackCommandId: string
}

interface ClasspathListenerResponse {
}
