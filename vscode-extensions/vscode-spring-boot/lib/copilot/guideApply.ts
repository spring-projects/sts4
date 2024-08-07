import { Uri, workspace, window } from "vscode";
import { SPRINGCLI } from "../Main";
import { getTargetGuideMardown } from "./util";
import { createConverter } from "vscode-languageclient/lib/common/protocolConverter";
import fs from "fs";


const CONVERTER = createConverter(undefined, true, true);
const CANCELLED = "Cancelled";

export async function applyLspEdit(uri: Uri) {
    try {
        if (!uri) {
            uri = await getTargetGuideMardown();
        }
        const lspEdit = await SPRINGCLI.guideLspEdit(uri);
        const workspaceEdit = await CONVERTER.asWorkspaceEdit(lspEdit);
        console.log(lspEdit);

        await Promise.all(workspaceEdit.entries().map(async ([uri, edits]) => {
            if (fs.existsSync(uri.fsPath)) {
                const doc = await workspace.openTextDocument(uri.fsPath);
                await window.showTextDocument(doc);
            }
        }));

        return await workspace.applyEdit(workspaceEdit, {
            isRefactoring: true
        });
    } catch (error) {
        if (error !== CANCELLED) {
            window.showErrorMessage(error);
        }
    }
}