import { InputBox, OpenDialogOptions, Uri, window, workspace } from "vscode";

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
    validate?: (v: string) => string | undefined,
    defaultValue?: string,
    placeholder?: string
}): Promise<string> {
    return new Promise<string>((resolve) => {
        const inputBox: InputBox = window.createInputBox();
        inputBox.title = opts.title;
        inputBox.placeholder = opts.placeholder;
        inputBox.prompt = opts.prompt;
        inputBox.value = opts.defaultValue;
        inputBox.ignoreFocusOut = true;
        inputBox.onDidChangeValue(() => {
            inputBox.validationMessage = opts.validate ? opts.validate(inputBox.value) : undefined;
        });
        inputBox.onDidAccept(() => {
            if (!inputBox.validationMessage) {
                inputBox.hide();
            }
        });
        inputBox.onDidHide(() => resolve(inputBox.value));
        inputBox.show();
    });
}
