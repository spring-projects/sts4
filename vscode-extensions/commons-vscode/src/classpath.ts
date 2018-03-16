'use strict';


import * as VSCode from 'vscode';
import { LanguageClient, RequestType } from 'vscode-languageclient';

export function registerClasspathService(client : LanguageClient) : void {

    let classpathListenerRequest = new RequestType<ClasspathListenerParams, ClasspathListenerResponse, void, void>("sts/addClasspathListener");
    client.onRequest(classpathListenerRequest, async (params: ClasspathListenerParams) => {
        return <ClasspathListenerResponse> await VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.addClasspathListener", params.callbackCommandId);
    });
}

interface ClasspathListenerParams {
    callbackCommandId: string
}

interface ClasspathListenerResponse {
}
