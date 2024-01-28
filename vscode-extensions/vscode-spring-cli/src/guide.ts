import { Uri } from "vscode";
import { getTargetGuideMardown } from "./utils";
import { CLI } from "./extension";

export async function handleGuideApply(uri: Uri) {
    if (!uri) {
        uri = await getTargetGuideMardown();
    }
    return CLI.guideApply(uri);
}