import { ActivatorOptions } from "@pivotal-tools/commons-vscode";
import { LanguageClient } from "vscode-languageclient/node";
import * as VSCode from 'vscode';
import * as path from "path";

interface RecipeDescriptor {
    name: string;
    displayName: string;
    description: string;
    tags: string[];
    estimatedEffortPerOccurrence: number;
    options: OptionDescriptor[];
    languages: string[];
    recipeList: RecipeDescriptor[];
    source: string;
}

interface OptionDescriptor {
    name: string;
    type: string;
    displayName: string;
    description: string;
    example: string;
    valid: string[] | undefined;
    required: boolean;
    value: any;
}

interface RecipeQuickPickItem extends VSCode.QuickPickItem{
    readonly id: string;
    selected: boolean;
    children: RecipeQuickPickItem[],
    readonly recipeDescriptor: RecipeDescriptor;
}

function getWorkspaceFolderName(file: VSCode.Uri): string {
    if (file) {
        const wf: VSCode.WorkspaceFolder = VSCode.workspace.getWorkspaceFolder(file);
        if (wf) {
            return wf.name;
        }
    }
    return '';
}

function getRelativePathToWorkspaceFolder(file: VSCode.Uri): string {
    if (file) {
        const wf: VSCode.WorkspaceFolder = VSCode.workspace.getWorkspaceFolder(file);
        if (wf) {
            return path.relative(wf.uri.fsPath, file.fsPath);
        }
    }
    return '';
}

async function getTargetPomXml(): Promise<VSCode.Uri> {
    if (VSCode.window.activeTextEditor) {
        const activeUri = VSCode.window.activeTextEditor.document.uri;
        if ("pom.xml" === path.basename(activeUri.path).toLowerCase()) {
            return activeUri;
        }
    }

    const candidates: VSCode.Uri[] = await VSCode.workspace.findFiles("**/pom.xml");
    if (candidates.length > 0) {
        if (candidates.length === 1) {
            return candidates[0];
        } else {
            return await VSCode.window.showQuickPick(
                candidates.map((c: VSCode.Uri) => ({ value: c, label: getRelativePathToWorkspaceFolder(c), description: getWorkspaceFolderName(c) })),
                { placeHolder: "Select the target project." },
            ).then(res => res && res.value);
        }
    }
    return undefined;
}

const ROOT_RECIPES_BUTTON: VSCode.QuickInputButton = {
    iconPath: new VSCode.ThemeIcon('home'),
    tooltip: 'Root Recipes'
}

const SUB_RECIPES_BUTTON: VSCode.QuickInputButton = {
    iconPath: new VSCode.ThemeIcon('type-hierarchy'),
    tooltip: 'Sub-Recipes'
}

async function liveHoverConnectHandler(uri: VSCode.Uri) {
    if (!uri) {
        uri = await getTargetPomXml();
    }
    const choices = await showCurrentPathQuickPick(VSCode.commands.executeCommand('sts/rewrite/list', uri.toString(true)).then((cmds: RecipeDescriptor[]) => cmds.map(convertToQuickPickItem)), []);
    const recipeDescriptors = choices.filter(i => i.selected).map(convertToRecipeDescriptor);
    if (recipeDescriptors.length) {
        const aggregateRecipeDescriptor = recipeDescriptors.length === 1 ? recipeDescriptors[0] : {
            name: `${recipeDescriptors.length} recipes`,
            displayName:  `${recipeDescriptors.length} recipes`,
            description: recipeDescriptors.map(d => d.description).join('\n'),
            tags: [...new Set<string>(recipeDescriptors.flatMap(d => d.tags))],
            languages: [...new Set<string>(recipeDescriptors.flatMap(d => d.languages))],
            options: [],
            recipeList: recipeDescriptors,
            estimatedEffortPerOccurrence: recipeDescriptors.filter(d => d.estimatedEffortPerOccurrence).map(d => d.estimatedEffortPerOccurrence).reduce((p, c) => p + c, 0)
        };
        if (aggregateRecipeDescriptor.estimatedEffortPerOccurrence === 0) {
            delete aggregateRecipeDescriptor.estimatedEffortPerOccurrence;
        }
        VSCode.commands.executeCommand('sts/rewrite/execute', uri.toString(true), aggregateRecipeDescriptor); 
    } else {
        VSCode.window.showErrorMessage('No Recipes were selected!');
    }
}

function convertToRecipeDescriptor(i: RecipeQuickPickItem): RecipeDescriptor {
    return {
        ...i.recipeDescriptor,
        recipeList: i.children.filter(c => c.selected).map(convertToRecipeDescriptor)
    };
}

function convertToQuickPickItem(i: RecipeDescriptor): RecipeQuickPickItem {
    return {
        id: i.name,
        label: i.displayName,
        detail: i.options.filter(o => !!o.value).map(o => `${o.name}: ${JSON.stringify(o.value)}`).join('\n\n'),
        description: i.description,
        selected: false,
        children: i.recipeList ? i.recipeList.map(convertToQuickPickItem) : [],
        buttons: i.recipeList && i.recipeList.length ? [ SUB_RECIPES_BUTTON ] : undefined,
        recipeDescriptor: i
    };
}

function showCurrentPathQuickPick(itemsPromise: Thenable<RecipeQuickPickItem[]>, itemsPath: RecipeQuickPickItem[]): Thenable<RecipeQuickPickItem[]> {
    const quickPick = VSCode.window.createQuickPick<RecipeQuickPickItem>();
    quickPick.title = 'Loading Recipes...';
    quickPick.canSelectMany = true;
    quickPick.busy = true;
    quickPick.show();
    return itemsPromise.then(items => {
        return new Promise((resolve, reject) => {
            let currentItems = items;
            let parent: RecipeQuickPickItem | undefined;
            itemsPath.forEach(p => {
                parent = currentItems.find(i => i === p);
                currentItems = parent.children;
            });
            quickPick.items = currentItems;
            if (itemsPath.length) {
                quickPick.buttons = [ ROOT_RECIPES_BUTTON ];
            }
            quickPick.selectedItems = currentItems.filter(i => i.selected);
            quickPick.onDidTriggerItemButton(e => {
                if (e.button === SUB_RECIPES_BUTTON) {
                    currentItems.forEach(i => i.selected = quickPick.selectedItems.includes(i));
                    itemsPath.push(e.item);
                    showCurrentPathQuickPick(Promise.resolve(items), itemsPath).then(resolve, reject);
                }
            });
            quickPick.onDidTriggerButton(b => {
                if (b === ROOT_RECIPES_BUTTON) {
                    currentItems.forEach(i => i.selected = quickPick.selectedItems.includes(i));
                    itemsPath.splice(0, itemsPath.length);
                    showCurrentPathQuickPick(Promise.resolve(items), itemsPath).then(resolve, reject);
                }
            });
            quickPick.onDidAccept(() => {
                currentItems.forEach(i => i.selected = quickPick.selectedItems.includes(i));
                if (itemsPath.length) {
                    itemsPath.pop();
                    showCurrentPathQuickPick(Promise.resolve(items), itemsPath).then(resolve, reject);
                } else {
                    quickPick.hide();
                    resolve(items);
                }
            });
            quickPick.onDidChangeSelection(selected => {
                currentItems.forEach(i => {
                    const isSelectedItem = selected.includes(i);
                    if (i.selected !== isSelectedItem) {
                        selectItemRecursively(i, isSelectedItem);
                    }
                });
                updateParentSelection(itemsPath.slice());
            });
            quickPick.title = 'Select Recipes';
            quickPick.busy = false;
        });
    });
}

function updateParentSelection(hierarchy: RecipeQuickPickItem[]): void {
    if (hierarchy.length) {
        const parent = hierarchy.pop();
        const isSelected = !!parent.children.find(i => i.selected);
        if (parent.selected !== isSelected) {
            parent.selected = isSelected;
            updateParentSelection(hierarchy)
        }    
    }
}

function selectItemRecursively(i: RecipeQuickPickItem, isSelectedItem: boolean) {
    i.selected = isSelectedItem;
    if (i.children) {
        i.children.forEach(c => selectItemRecursively(c, isSelectedItem));
    }
}

/** Called when extension is activated */
export function activate(
    client: LanguageClient,
    options: ActivatorOptions,
    context: VSCode.ExtensionContext
) {
    context.subscriptions.push(
        VSCode.commands.registerCommand('vscode-spring-boot.rewrite.list', liveHoverConnectHandler),
        VSCode.commands.registerCommand('vscode-spring-boot.rewrite.reload', () => VSCode.commands.executeCommand('sts/rewrite/reload'))
    );
}