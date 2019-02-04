import * as VSCode from "vscode";
import {LanguageClient, RequestType} from "vscode-languageclient";

export function registerJavaDataService(client : LanguageClient) : void {

    const javaTypeRequest = new RequestType<JavaDataParams, any, void, void>("sts/javaType");
    client.onRequest(javaTypeRequest, async (params: JavaDataParams) =>
        <any> await VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.type", params)
    );

    const javadocHoverLinkRequest = new RequestType<JavaDataParams, any, void, void>("sts/javadocHoverLink");
    client.onRequest(javadocHoverLinkRequest, async (params: JavaDataParams) =>
        <any> await VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.javadocHoverLink", params)
    );

    const javaLocationRequest = new RequestType<JavaDataParams, any, void, void>("sts/javaLocation");
    client.onRequest(javaLocationRequest, async (params: JavaDataParams) =>
        <any> await VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.location", params)
    );

    const javadocRequest = new RequestType<JavaDataParams, any, void, void>("sts/javadoc");
    client.onRequest(javadocRequest, async (params: JavaDataParams) =>
        <any> await VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.javadoc", params)
    );

}

interface JavaDataParams {
    projectUri: string;
    bindingKey: string;
}