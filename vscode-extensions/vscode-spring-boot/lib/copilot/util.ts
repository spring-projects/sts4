import path from "path";
import { Uri, WorkspaceFolder, version, window, workspace } from "vscode";
import fs from "fs";
import { SemVer } from "semver";

export async function getWorkspaceRoot(): Promise<Uri | undefined> {
    if (workspace.workspaceFolders && workspace.workspaceFolders.length) {
        if (workspace.workspaceFolders.length === 1) {
            return workspace.workspaceFolders[0].uri;
        } else {
            return await window.showQuickPick(
                workspace.workspaceFolders.map((c: WorkspaceFolder) => ({ value: c.uri, label: getRelativePathToWorkspaceFolder(c.uri), description: getWorkspaceFolderName(c.uri) })),
                { placeHolder: "Select the target project." },
            ).then(res => res && res.value);
        }
    }
}

export function getWorkspaceRootPath(): Uri | undefined {
    if (workspace.workspaceFolders && workspace.workspaceFolders.length) {
        return workspace.workspaceFolders[0].uri
    }
}

function getRelativePathToWorkspaceFolder(file: Uri): string {
    if (file) {
        const wf: WorkspaceFolder = workspace.getWorkspaceFolder(file);
        if (wf) {
            return path.relative(wf.uri.fsPath, file.fsPath);
        }
    }
    return '';
}

function getWorkspaceFolderName(file: Uri): string {
    if (file) {
        const wf: WorkspaceFolder = workspace.getWorkspaceFolder(file);
        if (wf) {
            return wf.name;
        }
    }
    return '';
}

export async function getTargetGuideMardown(): Promise<Uri> {
    if (window.activeTextEditor) {
        const activeUri = window.activeTextEditor.document.uri;
        if (/README-\S+.md/.test(path.basename(activeUri.path).toLowerCase())) {
            return activeUri;
        }
    }

    const candidates: Uri[] = await workspace.findFiles("**/README-*.md");
    if (candidates.length > 0) {
        if (candidates.length === 1) {
            return candidates[0];
        } else {
            return await window.showQuickPick(
                candidates.map((c: Uri) => ({ value: c, label: getRelativePathToWorkspaceFolder(c), description: getWorkspaceFolderName(c) })),
                { placeHolder: "Select the target project." },
            ).then(res => res && res.value);
        }
    }
    return undefined;
}

export async function writeResponseToFile(response: string, appName: string, selectedProject: string) {
    const readmeFilePath = path.resolve(selectedProject, `README-ai-${appName}.md`);
    if (fs.existsSync(readmeFilePath)) {
        try {
            fs.unlinkSync(readmeFilePath);
        } catch (ex) {
            throw new Error(`Could not delete readme file: ${readmeFilePath}, ${ex}`);
        }
    }

    try {
        fs.writeFileSync(readmeFilePath, response);
        return Uri.file(readmeFilePath);
    } catch (ex) {
        throw new Error(`Could not write readme file: ${readmeFilePath}, ${ex}`);
    }
}

export async function readResponseFromFile(uri: Uri) {
    return workspace.fs.readFile(uri);
}

export function isLlmApiReady(): boolean {
    return version.includes('insider') && new SemVer(version).compare(new SemVer("1.90.0-insider")) >= 0;
}