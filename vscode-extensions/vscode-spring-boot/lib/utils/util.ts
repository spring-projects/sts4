import path from "path";
import { Uri, WorkspaceFolder, window, workspace } from "vscode";

export function getExecutable(): string {
    return workspace.getConfiguration("spring-cli").get("executable") || "spring";
}

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