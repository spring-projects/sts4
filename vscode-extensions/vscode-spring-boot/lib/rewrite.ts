import { ActivatorOptions } from "@pivotal-tools/commons-vscode";
import { LanguageClient } from "vscode-languageclient/node";
import * as VSCode from 'vscode';
import * as path from "path";

interface RewriteCommandInfo {
    id: string;
    label: string;
    description: string
}

interface RewriteCommandQuickPickItem extends VSCode.QuickPickItem {
    id: string;
}

function getWorkspaceFolderName(file: VSCode.Uri): string {
    if (file) {
        const wf: VSCode.WorkspaceFolder = VSCode.workspace.getWorkspaceFolder(file);
        if (wf) {
            return wf.name;
        }
    }
    return "";
}

function getRelativePathToWorkspaceFolder(file: VSCode.Uri): string {
    if (file) {
        const wf: VSCode.WorkspaceFolder = VSCode.workspace.getWorkspaceFolder(file);
        if (wf) {
            return path.relative(wf.uri.fsPath, file.fsPath);
        }
    }
    return "";
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


async function liveHoverConnectHandler(uri: VSCode.Uri) {
    if (!uri) {
        uri = await getTargetPomXml();
    }
    const cmds: RewriteCommandInfo[] = await VSCode.commands.executeCommand('sts/rewrite/list', uri.toString(true));
    const choices: RewriteCommandQuickPickItem[] = cmds.map(d => {
        return {
            id: d.id,
            label: d.label,
            description: d.description,
        };
    });
    if (choices) {
        const picked = await VSCode.window.showQuickPick(choices);
        if (picked) {
            VSCode.commands.executeCommand(`sts/rewrite/recipe/${picked.id}`, uri.toString(true))
        }
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