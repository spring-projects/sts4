import { ProjectCatalog } from "./cli-types";
import { CLI } from "./extension";
import { window, QuickPickItem, Uri } from "vscode";
import { enterText } from "./utils";

interface ProjectCatalogQuickPick extends QuickPickItem {
    projectCatalog: ProjectCatalog;
}

const CUSTOM_CATALOG: ProjectCatalog = {
    name: "<Enter Custom Catalog>",
    url: "Provide URL",
    description: "Provide NAME, URL and optionally DESCRIPTION and TAGS"
}

export async function handleCatalogAdd() {

    let currentCatalogNames = [];

    // Select from available project catalog - currentle added project ctalogs
    let catalog = await pickCatalog(async () => {
        const [available, current] = await Promise.all([CLI.projectCatalogListAvailable(), CLI.projectCatalogList()]);
        const currentCatalogNames = current.map(c => c.name);
        return [CUSTOM_CATALOG, ...available.filter(a => !currentCatalogNames.includes(a.name))];
    });

    if (catalog === CUSTOM_CATALOG) {
        // No available catalog selected enter the catalog manually
        const name = await enterText({
            title: "Name",
            prompt: "Enter Name:",
            validate: v => {
                if (!/^\S+$/.test(v)) {
                    return "Invalid Project Catalog Name";
                }
                if (currentCatalogNames.includes(v)) {
                    return "Name alreasy exists"
                }
            }
        });
        const url = await enterText({
            title: "URL",
            prompt: "Enter URL:",
            placeholder: "https://github.com/my-org/my-project-catalog",
            validate: v => /[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)?/gi.test(v) ? "" : "Invalid URL value"
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
        catalog = {name, url, description, tags};
    }

    if (catalog) {
        return CLI.projectCatalogAdd(catalog);
    }
}

export async function handleCatalogRemove() {
    const catalog = await pickCatalog(CLI.projectCatalogList);
    if (catalog) {
        return CLI.projectCatalogRemove(catalog.name);
    }
}

async function pickCatalog(fetch: () => Thenable<ProjectCatalog[]>): Promise<ProjectCatalog | undefined> {
    return new Promise(async (resolve, reject) => {
        const quickPick = window.createQuickPick<ProjectCatalogQuickPick>();
        quickPick.busy = true;
        quickPick.title = "Loading Project Catalogs...";
        quickPick.canSelectMany = false;
        quickPick.show();
        const catalogs = await fetch();
    
        quickPick.items = catalogs.map(c => ({
            label: c.name,
            description: c.description,
            details: c.tags ? c.tags.join(", ") : undefined,
            projectCatalog: c
        }));
        quickPick.title = "Select Project Catalog";
        quickPick.busy = false;

        quickPick.onDidAccept(() => {
            resolve(quickPick.selectedItems.length ? quickPick.selectedItems[0].projectCatalog : undefined);
            quickPick.hide();
        });
    });
}