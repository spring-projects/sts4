import * as path from "path";
import { BootAddMetadata, BootNewMetadata } from "./cli-types";
import { CLI } from "./extension";
import { enterText, getTargetPomXml, mapProjectToQuickPick, openDialogForFolder } from "./utils";
import { Uri, commands, window, workspace } from 'vscode';
import fs from 'fs'

const OPEN_IN_NEW_WORKSPACE = "Open";
const OPEN_IN_CURRENT_WORKSPACE = "Add to Workspace";

export async function handleBootAdd(pom?: Uri): Promise<void> {
    const metadata: BootAddMetadata = {};
    pom = pom || await getTargetPomXml();
    if (pom) {
        metadata.targetFolder = path.dirname(pom.fsPath);
    }
    if (!metadata.targetFolder) {
        window.showErrorMessage("Spring-CLI Boot Add command requires a target project");
        return;
    }
    if (!metadata.catalog) {
        const fetchProjects = CLI.projectList().then(ps => ps.map(mapProjectToQuickPick));
        metadata.catalog = (await window.showQuickPick(fetchProjects, { canPickMany: false, ignoreFocusOut: true }))?.label;
    }
    if (metadata.catalog) {
        await CLI.bootAdd(metadata);
        const doc = await workspace.openTextDocument(path.join(metadata.targetFolder, `README-${metadata.catalog}.md`));
        await window.showTextDocument(doc);
    }
}

export async function handleBootNew(targetFolder?: string): Promise<void> {
    const metadata: BootNewMetadata = {};

    // Select parent folder
    metadata.targetFolder = targetFolder || (await openDialogForFolder({title: "Select Parent Folder"})).fsPath;

    // Select project type from the list of available types
    const fetchProjects = CLI.projectList().then(ps => ps.map(mapProjectToQuickPick));
    metadata.catalogId = (await window.showQuickPick(fetchProjects, { canPickMany: false }))?.label;

    if (!metadata.catalogId) {
        // Cancelled
        return;
    }

    try {
        metadata.name = await enterText({
            title: "Project Name",
            prompt: "Enter Project Name",
            defaultValue: metadata.name || metadata.catalogId,
            validate: async value => {
                if (!/^[a-z_][a-z0-9_]*(-[a-z_][a-z0-9_]*)*$/.test(value)) {
                    return "Invalid Project Name";
                }
                if (fs.existsSync(path.resolve(metadata.targetFolder, value))) {
                    return "Folder or file with such name exists under selected parent folder";
                }
            }
        });
        
        metadata.artifactId = await enterText({
            title: "Artifact Id",
            prompt: "Enter Artifact Id",
            defaultValue: metadata.name,
            validate: async value => (/^[a-z_][a-z0-9_]*(-[a-z_][a-z0-9_]*)*$/.test(value)) ? undefined : "Invalid Artifact Id"
        });
    
        metadata.groupId = await enterText({
            title: "Group Id",
            prompt: "Enter Group Id",
            defaultValue: "com.example",
            validate: async value => (/^[a-z_][a-z0-9_]*(\.[a-z0-9_]+)*$/.test(value)) ? undefined : "Invalid Group Id"
        });
    
        // Root package name
        metadata.rootPackage = await enterText({
            title: "Root Package Name",
            prompt: "Enter Root Package Name",
            defaultValue: `${metadata.groupId}.${metadata.name.toLowerCase().replace(/(-|_)+/g, ".")}`,
            validate: async value => (/^[a-z][a-z0-9_]*(\.[a-z0-9_]+)+[0-9a-z_]$/.test(value)) ? undefined : "Invalid Package Name"
        });
    } catch (error) {
        // Cancelled
        return;
    }


    // Create project and open in the workspace or new window
    if (metadata.name && metadata.catalogId && metadata.targetFolder) {
        await CLI.bootNew(metadata);

        // Open project either is the same workspace or new workspace
        const hasOpenFolder = workspace.workspaceFolders !== undefined || workspace.rootPath !== undefined;

        const pathToOpen = path.resolve(metadata.targetFolder, metadata.name);

        // Don't prompt to open projectLocation if it's already a currently opened folder
        if (hasOpenFolder && (workspace.workspaceFolders.some(folder => folder.uri.fsPath === pathToOpen) || workspace.rootPath === pathToOpen)) {
            return;
        }
        const choice = await specifyOpenMethod(hasOpenFolder, Uri.file(pathToOpen))

        if (choice === OPEN_IN_NEW_WORKSPACE) {
            commands.executeCommand("vscode.openFolder", Uri.file(pathToOpen), hasOpenFolder);
        } else if (choice === OPEN_IN_CURRENT_WORKSPACE) {
            if (!workspace.workspaceFolders.find((workspaceFolder) => workspaceFolder.uri && pathToOpen.startsWith(workspaceFolder.uri.fsPath))) {
                workspace.updateWorkspaceFolders(workspace.workspaceFolders.length, null, { uri: Uri.file(pathToOpen) });
            }
        }
    }

}


async function specifyOpenMethod(hasOpenFolder: boolean, projectLocation: Uri): Promise<string> {
    const candidates: string[] = [
        OPEN_IN_NEW_WORKSPACE,
        hasOpenFolder ? OPEN_IN_CURRENT_WORKSPACE : undefined,
    ].filter(Boolean);
    return await window.showInformationMessage(`Successfully generated. Location: ${projectLocation.fsPath}`, ...candidates);
}
