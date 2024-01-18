import { Uri, window } from "vscode";
import { enterText, mapProjectToQuickPick } from "./utils";

import { CLI } from "./extension";

export async function handleProjectAdd() {
    try {
        const currentProjectNames = (await CLI.projectList()).map(p => p.name);
        const name = await enterText({
            title: "Name",
            prompt: "Enter Name:",
            validate: v => {
                if (!/^\S+$/.test(v)) {
                    return "Invalid Project Catalog Name";
                }
                if (currentProjectNames.includes(v)) {
                    return "Name alreasy exists"
                }
            }
        });
        const url = await enterText({
            title: "URL",
            prompt: "Enter URL:",
            placeholder: "https://github.com/my-org/my-project",
            validate: v => {
                try {
                    Uri.parse(v, true);
                } catch (error) {
                    return "Invalid URL value"
                }
            }
        });
        const description = await enterText({
            title: "Description",
            prompt: "Enter Description:"
        });
        const tags = (await enterText({
            title: "Tags",
            prompt: "Enter Tags as strings separated by spaces and/or commas",
            placeholder: "java, spring, eureka, config"
        })).split(/(,)?\s+/);
    } catch (error) {
        // Ignore error - must have been cancelled
    }

}

export async function handleProjectRemove() {
    const name = (await window.showQuickPick(CLI.projectList().then(ps => ps.map(mapProjectToQuickPick)), {
        canPickMany: false,
        ignoreFocusOut: true,
    }))?.label;
    if (name) {
        return CLI.projectRemove(name);
    }
}