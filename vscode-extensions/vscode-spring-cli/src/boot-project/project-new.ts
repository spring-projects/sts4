import * as path from "path";
import { CliNewProjectMetadata, CliProjectType } from "../cli/types";
import { CLI } from "../extension";
import { openDialogForFolder } from "../utils";
import vscode, { InputBox, QuickPickItem, QuickPickItemKind } from 'vscode';

const OPEN_IN_NEW_WORKSPACE = "Open";
const OPEN_IN_CURRENT_WORKSPACE = "Add to Workspace";

export async function handleCommand(metadata: CliNewProjectMetadata) {
    if (!metadata.catalogType) {
        const item = await vscode.window.showQuickPick(CLI.getProjectTypes().map(mapProjectTypetoQuickPick), { canPickMany: false });
        if (item) {
            metadata.catalogType = item.label;
        }
    }

    await new Promise<void>((resolve, reject) => {
        const inputBox: InputBox = vscode.window.createInputBox();
        inputBox.title = 'Project Name';
        inputBox.placeholder = metadata.name || metadata.catalogType;
        inputBox.prompt = 'Enter project name';
        inputBox.value = metadata.name;
        inputBox.ignoreFocusOut = true;
        inputBox.onDidAccept(() => {
            metadata.name = inputBox.value;
            inputBox.hide();
        });
        inputBox.onDidHide(() => resolve());
        inputBox.show();
    });

    metadata.rootPackage = 'com.example.demo';

    metadata.targetFolder = (await openDialogForFolder({})).fsPath;

    if (metadata.name && metadata.catalogType && metadata.targetFolder) {
        vscode.window.showInformationMessage(`Folder="${metadata.targetFolder}" Command="spring boot new ${metadata.name} ${metadata.catalogType}"`)
        CLI.createProject(metadata);

        // Open project either is the same workspace or new workspace
        const hasOpenFolder = vscode.workspace.workspaceFolders !== undefined || vscode.workspace.rootPath !== undefined;

        const pathToOpen = path.resolve(metadata.targetFolder, metadata.name);

        // Don't prompt to open projectLocation if it's already a currently opened folder
        if (hasOpenFolder && (vscode.workspace.workspaceFolders.some(folder => folder.uri.fsPath === pathToOpen) || vscode.workspace.rootPath === pathToOpen)) {
            return;
        }
        const choice = await specifyOpenMethod(hasOpenFolder, vscode.Uri.file(pathToOpen))

        if (choice === OPEN_IN_NEW_WORKSPACE) {
            vscode.commands.executeCommand("vscode.openFolder", vscode.Uri.file(pathToOpen), hasOpenFolder);
        } else if (choice === OPEN_IN_CURRENT_WORKSPACE) {
            if (!vscode.workspace.workspaceFolders.find((workspaceFolder) => workspaceFolder.uri && pathToOpen.startsWith(workspaceFolder.uri.fsPath))) {
                vscode.workspace.updateWorkspaceFolders(vscode.workspace.workspaceFolders.length, null, { uri: vscode.Uri.file(pathToOpen) });
            }
        }
    }

}

function mapProjectTypetoQuickPick(metadata: CliProjectType): QuickPickItem {
    return {
        label: metadata.id,
        kind: QuickPickItemKind.Default,
        description: metadata.description,
        detail: metadata.tags.toString()
    };
}

async function specifyOpenMethod(hasOpenFolder: boolean, projectLocation: vscode.Uri): Promise<string> {
    const candidates: string[] = [
        OPEN_IN_NEW_WORKSPACE,
        hasOpenFolder ? OPEN_IN_CURRENT_WORKSPACE : undefined,
    ].filter(Boolean);
    return await vscode.window.showInformationMessage(`Successfully generated. Location: ${projectLocation.fsPath}`, ...candidates);
}