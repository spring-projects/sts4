import { InputBox, OpenDialogOptions, QuickPickItem, QuickPickItemKind, Uri, WorkspaceFolder, window, workspace} from "vscode";
import path from "path"
import { Project } from "./cli-types";
import debounce from "lodash.debounce";

export async function openDialogForFolder(customOptions: OpenDialogOptions): Promise<Uri> {
    const options: OpenDialogOptions = {
        canSelectFiles: false,
        canSelectFolders: true,
        canSelectMany: false,
        // default to current workspace folder (pick 1st one in multi-root)
        defaultUri: workspace.workspaceFolders && workspace.workspaceFolders.length > 0 ? workspace.workspaceFolders[0].uri : undefined
    };
    const result: Uri[] = await window.showOpenDialog(Object.assign(options, customOptions));
    if (result && result.length) {
        return Promise.resolve(result[0]);
    } else {
        return Promise.resolve(undefined);
    }
}

export function enterText(opts: {
    title: string,
    prompt: string,
    validate?: (v: string) => Promise<string | undefined>,
    defaultValue?: string,
    placeholder?: string
}): Promise<string> {
    return new Promise<string>((resolve, reject) => {
        const inputBox: InputBox = window.createInputBox();
        inputBox.title = opts.title;
        inputBox.placeholder = opts.placeholder;
        inputBox.prompt = opts.prompt;
        inputBox.value = opts.defaultValue;
        inputBox.ignoreFocusOut = true;
        inputBox.onDidChangeValue(debounce(async v => inputBox.validationMessage = opts.validate ? await opts.validate(inputBox.value) : undefined, 300));
        inputBox.onDidAccept(async () => {
            inputBox.validationMessage = opts.validate ? await opts.validate(inputBox.value) : undefined;
            if (!inputBox.validationMessage) {
                resolve(inputBox.value);
                inputBox.hide();
            }
        });
        inputBox.onDidHide(() => reject("cancelled"));
        inputBox.show();
    });
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

export async function getTargetPomXml(): Promise<Uri> {
    // if (window.activeTextEditor) {
    //     const activeUri = window.activeTextEditor.document.uri;
    //     if ("pom.xml" === path.basename(activeUri.path).toLowerCase()) {
    //         return activeUri;
    //     }
    // }

    const candidates: Uri[] = await workspace.findFiles("**/pom.xml");
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


export function mapProjectToQuickPick(project: Project): QuickPickItem {
    return {
        label: project.name,
        kind: QuickPickItemKind.Default,
        description: project.description
    };
}

export function getWorkspaceRoot(): Uri | undefined {
    if (workspace.workspaceFolders && workspace.workspaceFolders.length) {
        return workspace.workspaceFolders[0].uri
    }
}




