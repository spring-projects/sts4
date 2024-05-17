import { QuickPickItem, Uri, WorkspaceFolder, window, workspace } from "vscode";
import { enterText } from "./utils";
import { CLI } from "./extension";
import { homedir } from "os";
import { CommandInfo } from "./cli-types";
import { CANCELLED } from "./cli";

interface SubCommandQuickPickItem extends QuickPickItem {
    info?: CommandInfo;
}

interface ChoiceQuickPickItem extends QuickPickItem {
    value: string;
}

const GLOBAL: QuickPickItem = {
    label: "GLOBAL",
    description: "Globally available for all user projects",
    detail: homedir()
};

const LOCAL_SCOPE_DESCRIPTION = "Available only within selected workspace folder"

export async function handleCommandAdd(uri?: Uri) {

    // Enter CWD for CLI (global vs workspace folder local command)
    const cwd = await enterCwd(uri);

    // Enter URL of the repo for the command to add
    let url;
    try {
        url = await enterText({
            title: "URL",
            prompt: "Enter Repository URL",
            placeholder: "https://github.com/my-org/my-command",
            validate: async v => {
                try {
                    Uri.parse(v, true);
                } catch (error) {
                    return "Invalid URL value";
                }
            }
        });
    } catch (error) {
        // Cancelled - ignore the error
    }

    if (url) {
        CLI.commandAdd({url}, cwd);
    }
}

export async function handleCommandRemove(uri?: Uri) {
    try {
        // Enter CWD for CLI (global vs workspace folder local command)
        const cwd = await enterCwd(uri);
        // Select the command
        const command = await pickCommand(cwd);
        if (!command) {
            return;
        }
        // Select the subcommand
        const subcommand = (await pickSubCommand(cwd, command))?.label;
        if (!subcommand) {
            return;
        }
        return CLI.commandRemove({
            command,
            subcommand,
            cwd
        });
    } catch (error) {
        if (error !== CANCELLED) {
            window.showErrorMessage(error);
        }
    }
}

export async function handleCommandNew(uri?: Uri) {
    const cwd = await enterCwd(uri);
    const cmdName = await enterText({
        prompt: "Enter Command Name",
        title: "Command Name",
        defaultValue: "hello"
    });
    if (!cmdName) {
        return;
    }
    const subCmdName = await enterText({
        prompt: "Enter Sub-Command Name",
        title: "Sub-Command Name",
        defaultValue: "new"
    });
    if (!subCmdName) {
        return;
    }
    return CLI.commandNew(cwd, cmdName, subCmdName);
}

export async function handleCommandExecute(uri?: Uri) {
    // Enter CWD for CLI (global vs workspace folder local command)
    const cwd = await enterCwd(uri);
    // Select the command
    const command = await pickCommand(cwd);
    if (!command) {
        return;
    }
    // Select the subcommand
    const subcommand = (await pickSubCommand(cwd, command));
    if (!subcommand) {
        return;
    }

    const params = {};

    try {
        if (Array.isArray(subcommand?.info?.options)) {
            for (const o of subcommand.info.options) {
                let value;
                if (o.choices) {
                    const quickPickItems = Object.keys(o.choices).map(s => ({
                        label: s,
                        value: o.choices[s]
                    }) as ChoiceQuickPickItem);
                    value = (await window.showQuickPick(quickPickItems, { canPickMany: false, ignoreFocusOut: true })).value;
                } else {
                    value = await enterText({
                        title: o.paramLabel || o.name,
                        defaultValue: o.defaultValue,
                        placeholder: o.defaultValue,
                        prompt: `Enter ${o.paramLabel || o.name}${o.description ? " - " : ""}${o.description}`
                    });
                }
                if (value) {
                    params[o.name] = value;
                }
            } 
        }
    
        return CLI.commandExecute({
            command,
            subcommand: subcommand.label,
            params
        }, cwd);
    } catch (error) {
        if (error !== CANCELLED) {
            window.showErrorMessage(error);
        }
    }
}

function mapFolderToQuickPickItem(folder: WorkspaceFolder): QuickPickItem {
    return {
        label: folder.name,
        description: LOCAL_SCOPE_DESCRIPTION,
        detail: folder.uri.fsPath
    };
}

async function enterCwd(uri?: Uri): Promise<string> {
    const commandScopes = [ GLOBAL ];
    if (uri) {
        const folder = workspace.getWorkspaceFolder(uri);
        if (folder) {
            commandScopes.push(mapFolderToQuickPickItem(folder));
        }
    } else {
        commandScopes.push(...workspace.workspaceFolders.map(mapFolderToQuickPickItem));
    }

    return commandScopes.length > 1 ? (await window.showQuickPick(commandScopes, { canPickMany: false, ignoreFocusOut: true }))?.detail : GLOBAL.detail;
}

async function pickCommand(cwd: string): Promise<string> {
    return await window.showQuickPick(CLI.commandList(cwd), { canPickMany: false, ignoreFocusOut: true});
}

async function pickSubCommand(cwd: string, command: string): Promise<SubCommandQuickPickItem> {
    const deferredItems = CLI.commandList(cwd, command).then(async subcommands => {
        return await Promise.all(subcommands.map(async sc => {
            const qp: SubCommandQuickPickItem = {
                label: sc,
            };
            try {
                qp.info = await CLI.commandInfo(cwd, command, sc);
                qp.description = qp.info.description;
            } catch (error) {
                window.showWarningMessage(`Could not fetch info for CLI command: '${command} ${sc}'`);
            }
            return qp;
        }));
    });
    return await window.showQuickPick(deferredItems, {canPickMany: false, ignoreFocusOut: true});
}