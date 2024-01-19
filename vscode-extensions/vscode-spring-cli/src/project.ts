import { Uri, window } from "vscode";
import { enterText, mapProjectToQuickPick } from "./utils";

import { CLI } from "./extension";

export async function handleProjectAdd() {
    try {
        const currentProjectNamesPromise = CLI.projectList().then(projects => projects.map(p => p.name));
        const name = await enterText({
            title: "Name",
            prompt: "Enter Name:",
            validate: async v => {
                if (!/^\S+$/.test(v)) {
                    return "Invalid Project Catalog Name";
                }
                if ((await currentProjectNamesPromise).includes(v)) {
                    return "Name alreasy exists"
                }
            }
        });
        const url = await enterText({
            title: "URL",
            prompt: "Enter URL:",
            placeholder: "https://github.com/my-org/my-project",
            validate: async v => {
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
        if (name && url) {
            return CLI.projectAdd({
                name,
                url,
                description,
                tags
            });
        }
    } catch (error) {
        // Ignore error - must have been cancelled
    }

}

export async function handleProjectRemove() {
    const deferred = CLI.projectList().then(ps => ps.map(mapProjectToQuickPick));
    const name = (await window.showQuickPick(deferred, {
        canPickMany: false,
        ignoreFocusOut: true,
    }))?.label;
    if (name) {
        return CLI.projectRemove(name);
    }
}