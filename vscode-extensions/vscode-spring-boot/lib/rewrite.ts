import { ActivatorOptions } from "@pivotal-tools/commons-vscode";
import { LanguageClient } from "vscode-languageclient/node";
import * as VSCode from 'vscode';
import * as path from "path";

interface RewriteCommandInfo {
    id: string;
    label: string;
    detail?: string;
    children: RewriteCommandInfo[];
    selected: boolean;
}

interface RecipeQuickPickItem extends VSCode.QuickPickItem{
    id: string;
    children: RecipeQuickPickItem[];
    selected: boolean;
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
    const cmds: RewriteCommandInfo[] = await VSCode.commands.executeCommand('sts/rewrite/list', uri.toString(true));
    const choices = cmds.map(convertToQuickPickItem);
    await showCurrentPathQuickPick(choices, []);
    const recipesInfo = choices.filter(i => i.selected).map(convertToRecipeDescriptor);
    if (recipesInfo.length) {
        VSCode.commands.executeCommand('sts/rewrite/execute', uri.toString(true), recipesInfo); 
    } else {
        VSCode.window.showErrorMessage('No Recipes were selected!');
    }
}

function convertToRecipeDescriptor(i: RecipeQuickPickItem): RewriteCommandInfo {
    return {
        id: i.id,
        label: i.label,
        selected: i.selected,
        children: i.children.map(convertToRecipeDescriptor)
    };
}

function convertToQuickPickItem(i: RewriteCommandInfo): RecipeQuickPickItem {
    return {
        id: i.id,
        label: i.label,
        detail: i.detail,
        selected: i.selected,
        children: i.children ? i.children.map(convertToQuickPickItem) : [],
        buttons: i.children && i.children.length ? [ SUB_RECIPES_BUTTON ] : undefined,
    };
}

function showCurrentPathQuickPick(items: RecipeQuickPickItem[], itemsPath: RecipeQuickPickItem[]): Promise<void> {
    return new Promise((resolve, reject) => {
        let currentItems = items;
        let parent: RecipeQuickPickItem | undefined;
        itemsPath.forEach(p => {
            parent = currentItems.find(i => i === p);
            currentItems = parent.children;
        });
        const quickPick = VSCode.window.createQuickPick<RewriteCommandInfo & VSCode.QuickPickItem>();
        quickPick.items = currentItems;
        quickPick.title = 'Select Recipes';
        quickPick.canSelectMany = true;
        if (itemsPath.length) {
            quickPick.buttons = [ ROOT_RECIPES_BUTTON ];
        }
        quickPick.selectedItems = currentItems.filter(i => i.selected);
        quickPick.onDidTriggerItemButton(e => {
            if (e.button === SUB_RECIPES_BUTTON) {
                currentItems.forEach(i => i.selected = quickPick.selectedItems.includes(i));
                itemsPath.push(e.item);
                showCurrentPathQuickPick(items, itemsPath).then(resolve, reject);
            }
        });
        quickPick.onDidTriggerButton(b => {
            if (b === ROOT_RECIPES_BUTTON) {
                currentItems.forEach(i => i.selected = quickPick.selectedItems.includes(i));
                itemsPath.splice(0, itemsPath.length);
                showCurrentPathQuickPick(items, itemsPath).then(resolve, reject);
            }
        });
        quickPick.onDidAccept(() => {
            currentItems.forEach(i => i.selected = quickPick.selectedItems.includes(i));
            if (itemsPath.length) {
                itemsPath.pop();
                showCurrentPathQuickPick(items, itemsPath).then(resolve, reject);
            } else {
                quickPick.hide();
                resolve();
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
        quickPick.show();
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
        VSCode.commands.registerCommand('vscode-spring-boot.rewrite.list', liveHoverConnectHandler)
    );
}