import { Uri, commands } from "vscode";
import { getTargetGuideMardown, readResponseFromFile } from "./util";

export async function applyLspEdit(uri: Uri) {
    if (!uri) {
        uri = await getTargetGuideMardown();
    }
    const fileContent = (await readResponseFromFile(uri)).toString();
    await commands.executeCommand("sts/copilot/agent/lspEdits", uri.toString(), fileContent);
}