import { ProjectCatalog } from "./cli-types";
import { CLI } from "./extension";
import { window, QuickPickItem, Uri } from "vscode";
import { enterText } from "./utils";

interface ProjectCatalogQuickPickItem extends QuickPickItem {
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
    const itemsPromise = Promise.all([CLI.projectCatalogListAvailable(), CLI.projectCatalogList()]).then(([available, current]) => {
        const currentCatalogNames = current.map(c => c.name);
        return [CUSTOM_CATALOG, ...available.filter(a => !currentCatalogNames.includes(a.name))].map(mapCatalogToQuickPickItem);
    });
    let catalog = (await window.showQuickPick(itemsPromise, { ignoreFocusOut: true, canPickMany: false}))?.projectCatalog;

    if (catalog === CUSTOM_CATALOG) {
        // No available catalog selected enter the catalog manually
        const name = await enterText({
            title: "Name",
            prompt: "Enter Name:",
            validate: async v => {
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
        catalog = {name, url, description, tags};
    }

    if (catalog) {
        return CLI.projectCatalogAdd(catalog);
    }
}

export async function handleCatalogRemove() {
    const itemsPromise = CLI.projectCatalogList().then(catalogs => catalogs.map(mapCatalogToQuickPickItem));
    const catalog = (await window.showQuickPick(itemsPromise, { ignoreFocusOut: true, canPickMany: false}))?.projectCatalog;
    if (catalog) {
        return CLI.projectCatalogRemove(catalog.name);
    }
}

function mapCatalogToQuickPickItem(c: ProjectCatalog): ProjectCatalogQuickPickItem {
    return {
        label: c.name,
        description: c.description,
        projectCatalog: c
    };
}
