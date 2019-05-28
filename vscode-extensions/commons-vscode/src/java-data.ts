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

    const javaSearchTypes = new RequestType<JavaSearchParams, any, void, void>("sts/javaSearchTypes");
    client.onRequest(javaSearchTypes, async (params: JavaSearchParams) =>
        <any> await VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.search.types", params)
    );

    const javaSearchPackages = new RequestType<JavaSearchParams, any, void, void>("sts/javaSearchPackages");
    client.onRequest(javaSearchPackages, async (params: JavaSearchParams) =>
        <any> await VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.search.packages", params)
    );

    const javaSubTypes = new RequestType<JavaTypeHierarchyParams, any, void, void>("sts/javaSubTypes");
    client.onRequest(javaSubTypes, async (params: JavaTypeHierarchyParams) =>
        <any> await VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.hierarchy.subtypes", params)
    );

    const javaSuperTypes = new RequestType<JavaTypeHierarchyParams, any, void, void>("sts/javaSuperTypes");
    client.onRequest(javaSuperTypes, async (params: JavaTypeHierarchyParams) =>
        <any> await VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.hierarchy.supertypes", params)
    );

    const javaCodeCompletion = new RequestType<JavaCodeCompleteParams, any, void, void>('sts/javaCodeComplete');
    client.onRequest(javaCodeCompletion, async (params: JavaCodeCompleteParams) =>
        <any> await VSCode.commands.executeCommand("java.execute.workspaceCommand", "sts.java.code.completions", params)
    );
}

interface JavaDataParams {
    projectUri: string;
    bindingKey: string;
    lookInOtherProjects?: boolean;
}

interface JavaSearchParams {
    projectUri: string;
    term: string;
    searchType: string;
    includeBinaries: boolean;
    includeSystemLibs: boolean;
    timeLimit: number;
}

interface JavaTypeHierarchyParams {
    projectUri?: string;
    fqName: string;
    includeFocusType: boolean;
}

interface JavaCodeCompleteParams {
    projectUri: string;
    prefix: string;
    includeTypes: boolean;
    includePackages: boolean;
}