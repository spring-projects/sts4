import { OpenDialogOptions, Uri, window, workspace } from "vscode";

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
