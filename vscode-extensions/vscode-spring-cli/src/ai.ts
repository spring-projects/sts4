import { Uri, commands, window } from "vscode";
import { CLI } from "./extension";
import { enterText, getTargetPomXml } from "./utils";
import path from "path";
import { handleGuideApplyWorkspaceEdit } from "./guide";
import { CANCELLED } from "./cli";

export async function handleAiAdd(pom: Uri) {
    let question: string;
    try {
        if (!pom) {
            pom = await getTargetPomXml();
        }
        question = await enterText({
            title: "Question",
            prompt: "Enter question to LLM",
        });
    } catch (error) {
        // ignore: cancellation via UI
    }
    try {
        if (question.trim().length > 0) {
            const uri = await CLI.aiAdd(question, path.dirname(pom.fsPath));
            await commands.executeCommand("markdown.showPreview", uri);
            if ("Yes" === await window.showInformationMessage(`Apply guide '${path.basename(uri.fsPath)}' to the project?`, "Yes", "No")) {
                handleGuideApplyWorkspaceEdit(uri);
            }    
        }
    } catch (error) {
        if (error !== CANCELLED) {
            window.showErrorMessage(error);
        }
    }
}
