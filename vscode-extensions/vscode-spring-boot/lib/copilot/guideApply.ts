import { Uri, workspace, window, commands, ProgressLocation } from "vscode";
import { getTargetGuideMardown, readResponseFromFile } from "./util";
import { createConverter } from "vscode-languageclient/lib/common/protocolConverter";
import fs from "fs";
import path from "path";


const CONVERTER = createConverter(undefined, true, true);
const CANCELLED = "Cancelled";

export async function applyLspEdit(uri: Uri) {
    try {
        if (!uri) {
            uri = await getTargetGuideMardown();
        }

        window.withProgress({
            location: ProgressLocation.Window,
            title: "Copilot agent",
            cancellable: true
        }, async (progress, cancellation) => {
            progress.report({ message: "applying edits..." });
            const fileContent = (await readResponseFromFile(uri)).toString();
            const lspEdit = await commands.executeCommand("sts/copilot/agent/lspEdits", uri.toString(), fileContent);
            const workspaceEdit = await CONVERTER.asWorkspaceEdit(lspEdit);
            

            await Promise.all(workspaceEdit.entries().map(async ([uri, edits]) => {
                console.log(edits);
                if (fs.existsSync(uri.fsPath)) {
                    const doc = await workspace.openTextDocument(uri.fsPath);
                    await window.showTextDocument(doc);
                }
            }));

            return await workspace.applyEdit(workspaceEdit, {
                isRefactoring: true
            });
    });
    } catch (error) {
        if (error !== CANCELLED) {
            window.showErrorMessage(error);
        }
    }
}