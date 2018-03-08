'use strict';


import * as VSCode from 'vscode';
import { LanguageClient, RequestType } from 'vscode-languageclient';

export function registerClasspathService(client : LanguageClient) : void {
    let classpathRequest = new RequestType<ClasspathParams, ClasspathResponse, void, void>("sts/classpath");

    client.onRequest(classpathRequest, async (params: ClasspathParams) => {
        return await executeClasspathCommand(params.resourceUri);
    });
}

async function executeClasspathCommand(resourceUri : string) : Promise<ClasspathResponse> {
    return <ClasspathResponse> (await VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.resolveClasspath", resourceUri));
}

interface ClasspathResponse {
    entries: ClasspathEntry[],
    defaultOutputFolder : string
}

interface ClasspathParams {
    resourceUri: string
}

interface ClasspathEntry {
    kind : string,
    path : string
}