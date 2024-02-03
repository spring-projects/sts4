import { Uri, window, workspace } from "vscode";
import { getTargetGuideMardown } from "./utils";
import { CLI } from "./extension";
import { createConverter } from "vscode-languageclient/lib/common/protocolConverter";
import fs from "fs";

const CONVERTER = createConverter(undefined, true, true);
export async function handleGuideApply(uri: Uri) {
    if (!uri) {
        uri = await getTargetGuideMardown();
    }
    return CLI.guideApply(uri);
}

export async function handleGuideRun(uri: Uri) {
    if (!uri) {
        uri = await getTargetGuideMardown();
    }
    const lspEdit = await CLI.guideLspEdit(uri);
    const workspaceEdit = await CONVERTER.asWorkspaceEdit(lspEdit);

	// This is some sort of workaround for undo
	// If editors for existing doc not opened then undo isn't properly working
    await Promise.all(workspaceEdit.entries().map(async ([uri, edits]) => {
        if (fs.existsSync(uri.fsPath)) {
            const doc = await workspace.openTextDocument(uri.fsPath);
            await window.showTextDocument(doc);
        }
    }));
    
    return await workspace.applyEdit(workspaceEdit, {
        isRefactoring: true
    });
}