'use strict';


import * as VSCode from 'vscode';
import { LanguageClient, RequestType } from 'vscode-languageclient';

export function registerProjectService(client : LanguageClient) : void {
    let projectRequest = new RequestType<string, ProjectResponse, void, void>("sts/project");

    client.onRequest(projectRequest,  (uri: string) => {
        return  executeProjectCommand(uri);
    });
}

async function executeProjectCommand(resourceUri : string) : Promise<ProjectResponse> {
    return <ProjectResponse> (await VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.resolveProject", resourceUri));
}

interface ProjectResponse {
    name: string,
    uri : string
}