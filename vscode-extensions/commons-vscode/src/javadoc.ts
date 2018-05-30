'use strict';


import * as VSCode from 'vscode';
import { LanguageClient, RequestType } from 'vscode-languageclient';

export function registerJavadocService(client : LanguageClient) : void {

    let addRequest = new RequestType<JavadocParams, JavadocResponse, void, void>("sts/javadoc");
    client.onRequest(addRequest, async (params: JavadocParams) =>
        <JavadocResponse> await VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.javadoc", params)
    );

}

interface JavadocParams {
    projectUri: string;
    bindingKey: string;
}

interface JavadocResponse {
    content: string;
}
