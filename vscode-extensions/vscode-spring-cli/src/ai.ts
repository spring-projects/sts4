import { Uri, commands, window } from "vscode";
import { CLI } from "./extension";
import { enterText, getTargetPomXml } from "./utils";
import path from "path";
import { handleGuideApply } from "./guide";

export async function handleAiAdd(pom: Uri) {
    if (!pom) {
        pom = await getTargetPomXml();
    }
    const question = await enterText({
        title: "Question",
        prompt: "Enter question to LLM",
    });
    const uri = await CLI.aiAdd(question, path.dirname(pom.fsPath));
    await commands.executeCommand("markdown.showPreview", uri);
    if ("Yes" === await window.showInformationMessage(`Apply guide '${path.basename(uri.fsPath)}' to the project?`, "Yes", "No")) {
        handleGuideApply(uri);
    }
}
