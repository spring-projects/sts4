import { BootAddMetadata, BootNewMetadata, Project, ProjectCatalog, CommandAddMetadata, CommandRemoveMetadata, CommandInfo, CommandExecuteMetadata } from "./cli-types";
import { CancellationToken, ProcessExecution, ProgressLocation, ProviderResult, ShellExecution, Task, TaskProvider, TaskScope, Uri, tasks, window, workspace } from "vscode";
import cp from "child_process";
import { homedir } from "os";
import { getWorkspaceRoot } from "./utils";
import path from "path";
import fs from "fs";
import yaml from "js-yaml";
import { WorkspaceEdit } from "vscode-languageclient";

export const SPRING_CLI_TASK_TYPE = 'spring-cli';

export const CANCELLED = "Cancelled";

export class Cli {

    get executable(): string {
        return workspace.getConfiguration("spring-cli").get("executable") || "spring";
    }

    projectList() : Promise<Project[]> {
        return this.fetchJson("Fetching projects...", undefined, ["project", "list", "--json"]);
    }

    projectCatalogList(): Promise<ProjectCatalog[]> {
        return this.fetchJson("Fetching Catalogs...", undefined, ["project-catalog", "list", "--json"]);
    }

    projectCatalogListAvailable(): Promise<ProjectCatalog[]> {
        return this.fetchJson("Fetching Available Catalogs...", undefined, ["project-catalog", "list-available", "--json"]);
    }

    guideApply(uri: Uri, cwd?: string) {
        const args = [
            "guide",
            "apply",
            "--file",
            uri.fsPath
        ];
        return this.exec("Applying guide", uri.fsPath, args, cwd || path.dirname(uri.fsPath));
    }

    guideLspEdit(uri: Uri, cwd?: string): Promise<WorkspaceEdit> {
        const args = [
            "guide",
            "apply",
            "--lsp-edit",
            "--file",
            uri.fsPath
        ];
        return this.fetchJson("Applying guide", uri.fsPath, args, cwd || path.dirname(uri.fsPath), true);
    }

    aiAdd(question: string, cwd: string): Thenable<Uri> {

        const args = [
            "ai",
            "add",
            "--preview",
            "true",
            "--description",
            `"${question}"`
        ];

        return window.withProgress({
            location: ProgressLocation.Window,
            cancellable: true,
            title: "Add from AI",
        }, (progress, cancellation) => {

            progress.report({ message: question });
            
            return new Promise<Uri>(async (resolve, reject) => {
                if (cancellation.isCancellationRequested) {
                    reject(CANCELLED);
                }
                const processOpts = { cwd: cwd || getWorkspaceRoot()?.fsPath || homedir() };
                const process = this.executable.endsWith(".jar") ? await cp.exec(`java -jar ${this.executable} ${args.join(" ")}`, processOpts) : await cp.exec(`${this.executable} ${args.join(" ")}`, processOpts);
                cancellation.onCancellationRequested(() => process.kill());
                const errorMessageChunks = [];
                let guideFileName;
                let errorMessageInStdOut;
                process.stdout.on("data", s => {
                    const res = /README-\S+.md/.exec(s);
                    if (res && res.length) {
                        guideFileName = res[0].trim();
                    } else {
                        errorMessageInStdOut = s;
                    }
                });
                process.stderr.on("data", s => errorMessageChunks.push(s))
                process.on("exit", (code) => {
                    if (code) {
                        if (cancellation.isCancellationRequested) {
                            reject(CANCELLED);
                        } else {
                            const errorMessage = (errorMessageChunks.length == 0 ? errorMessageInStdOut : errorMessageChunks.join()) || "";
                            reject(`Failed to get response from LLM. ${errorMessage}`);
                        }
                    } else {
                        resolve(Uri.file(path.join(cwd, guideFileName)));
                    }
                });
            });
        });

    }

    commandAdd(metadata: CommandAddMetadata, cwd?: string) {
        const args = [
            "command",
            "add",
            "--from",
            metadata.url
        ];
        this.exec("Add Command", metadata.url, args, cwd || homedir());
    }

    commandRemove(metadata: CommandRemoveMetadata) {
        const args = [
            "command",
            "remove",
            "--commandName",
            metadata.command,
            "--subCommandName",
            metadata.subcommand
        ];
        this.exec("Remove Command", `'${metadata.command} ${metadata.subcommand}'`, args, metadata.cwd);
    }

    commandList(cwd: string, command?: string): Promise<string[]> {
        return new Promise(async(resolve, reject) => {
            let p = path.join(cwd, ".spring", "commands");
            if (command) {
                p = path.join(p, command);
            }
            fs.readdir(p, { withFileTypes: true }, (error, files) => {
                if (error) {
                    reject(error.message);
                } else {
                    resolve(files.filter(d => d.isDirectory()).map(d => d.name));
                }
            })
        });
    }

    commandInfo(cwd: string, command: string, subcommand: string): Promise<CommandInfo> {
        return new Promise((resolve, reject) => {
            try {
                const parentFolder = path.join(cwd, ".spring", "commands", command, subcommand);
                let file;
                if (fs.existsSync(path.join(parentFolder, "command.yaml"))) {
                    file = path.join(parentFolder, "command.yaml");
                } else if (fs.existsSync(path.join(parentFolder, "command.yml"))) {
                    file = path.join(parentFolder, "command.yml");
                }
                if (file) {
                    const content = fs.readFileSync(file, "utf-8");
                    const obj = yaml.load(content, {json: true}) as any;
                    if (typeof obj === 'object' && obj?.command) {
                        resolve(obj.command as CommandInfo);
                    } else {
                        reject(`Unable to read command '${command} ${subcommand}' info`);
                    }
                }
            } catch (error) {
                reject(error);
            }
        });
    }

    commandNew(cwd: string, commandName?: string, subCommandName?: string) {
        const args = [
            "command",
            "new",
        ];
        if (commandName) {
            args.push("--command-name")
            args.push(commandName);
        }
        if (subCommandName) {
            args.push("--sub-command-name")
            args.push(subCommandName);
        }
        return this.exec("New Command", undefined, args, cwd);
    }

    commandExecute(metadata: CommandExecuteMetadata, cwd: string) {
        const args = [
            metadata.command,
            metadata.subcommand,
        ];
        Object.keys(metadata.params).forEach(p => {
            args.push(`--${p}`);
            args.push(metadata.params[p]);
        });
        return this.exec("Executing Command", `'${metadata.command} ${metadata.subcommand}'`, args, cwd);
    }

    projectCatalogAdd(catalog: ProjectCatalog): Promise<void> {
        const args = [
            "project-catalog",
            "add",
            "--name",
            catalog.name,
            "--url",
            catalog.url
        ];
        if (catalog.description) {
            args.push("--description");
            args.push(catalog.description);
        }
        if (catalog.tags) {
            args.push("--tags");
            args.push(catalog.tags.join(","));
        }
        return this.exec("Add Project Catalog", `'${catalog.name}'`, args);
    }

    projectCatalogRemove(name: string): Promise<void> {
        const args = [
            "project-catalog",
            "remove",
            "--name",
            name,
        ];
        return this.exec("Remove Project Catalog", `'${name}'`, args);
    }

    projectAdd(project: Project) {
        const args = [
            "project",
            "add",
            "--name",
            project.name,
            "--url",
            project.url,
        ];
        if (project.description) {
            args.push("--description");
            args.push(project.description);
        }
        if (project.tags) {
            args.push("--tags");
            args.push(project.tags.join(","));
        }
        return this.exec("Add Project", `'${project.name}'`, args);
    }

    projectRemove(name: string) {
        const args = [
            "project",
            "remove",
            "--name",
            name
        ];
        return this.exec("Remove Project", `'${name}'`, args);
    }

    bootNew(metadata: BootNewMetadata): Promise<void> {
        const args = [
            "boot",
            "new",
            "--name",
            metadata.name,
            "--from",
            metadata.catalogId
        ];
        if (metadata.groupId) {
            args.push("--group-id")
            args.push(metadata.groupId);
        }
        if (metadata.artifactId) {
            args.push("--artifact-id")
            args.push(metadata.artifactId);
        }
        if (metadata.rootPackage) {
            args.push("--package-name");
            args.push(metadata.rootPackage);
        }
        return this.exec("New Boot Project", `'${metadata.catalogId}'`, args, metadata.targetFolder);
    }

    bootAdd(metadata: BootAddMetadata): Promise<void> {
        const args = [
            "boot",
            "add",
            "--from",
            metadata.catalog
        ];
        return this.exec("Add to Boot Project", `'${metadata.catalog}'`, args, metadata.targetFolder);
    }

    private async exec(title: string, message: string, args: string[], cwd?: string): Promise<void> {

        return window.withProgress({
            location: ProgressLocation.Window,
            cancellable: true,
            title
        }, (progress, cancellation) => {
            
            progress.report({message});
        
            return new Promise<void>(async (resolve, reject) => {
                const processOpts = { cwd: cwd || getWorkspaceRoot()?.fsPath || homedir() };

                // const process = this.executable.endsWith(".jar") ? new ShellExecution("java", [ "-jar", this.executable,  ...args], processOpts) : new ShellExecution(this.executable, args, processOpts)
                const process = this.executable.endsWith(".jar") ? new ProcessExecution("java", [ "-jar", this.executable,  ...args], processOpts) : new ProcessExecution(this.executable, args, processOpts);
                const task = new Task({ type: SPRING_CLI_TASK_TYPE }, cwd ? workspace.getWorkspaceFolder(Uri.file(cwd)) : TaskScope.Global, `${title}: ${message}`, SPRING_CLI_TASK_TYPE, process);
                const taskExecution = await tasks.executeTask(task);
                if (cancellation.isCancellationRequested) {
                    reject();
                }
                const cancelListener = cancellation.onCancellationRequested(() => {
                    cancelListener.dispose();
                    reject();
                });

                const listener = tasks.onDidEndTaskProcess(e => {
                    if (e.execution === taskExecution) {
                        listener.dispose();
                        resolve();
                    }
                });
            });
        
        });
    }

    private async fetchJson<T>(title: string, message: string, args: string[], cwd?: string, omitJsonParam?: boolean) : Promise<T> {

        return window.withProgress({
            location: ProgressLocation.Window,
            cancellable: true,
            title
        }, (progress, cancellation) => {
            
            if (message) {
                progress.report({message});
            }

            return new Promise<T>(async (resolve, reject) => {
                if (cancellation.isCancellationRequested) {
                    reject(CANCELLED);
                }
                const processOpts = { cwd: cwd || getWorkspaceRoot()?.fsPath || homedir() };
                const process = this.executable.endsWith(".jar") ? await cp.exec(`java -jar ${this.executable} ${args.join(" ")}`, processOpts) : await cp.exec(`${this.executable} ${args.join(" ")} ${omitJsonParam ? "" : "--json"}`, processOpts);
                cancellation.onCancellationRequested(() => process.kill());
                const dataChunks: string[] = [];
                process.stdout.on("data", s => dataChunks.push(s));
                process.on("exit", (code) => {
                    if (code) {
                        if (cancellation.isCancellationRequested) {
                            reject(CANCELLED);
                        } else {
                            reject(`Failed to fetch data: ${dataChunks.join()}`);
                        }
                    } else {
                        try {
                            resolve(JSON.parse(dataChunks.join()) as T);
                        } catch (error) {
                            reject(error);
                        }
                    }
                });
            });
        });
    }

}

export class CliTaskProvider implements TaskProvider {

    constructor(private cli: Cli) {}

    provideTasks(token: CancellationToken): ProviderResult<Task[]> {
        return [];
    }

    resolveTask(task: Task, token: CancellationToken): ProviderResult<Task> {
        if (task.definition.type === SPRING_CLI_TASK_TYPE) {
            const processOpts = { cwd: task.definition.cwd || getWorkspaceRoot()?.fsPath || homedir() };
            task.execution = this.cli.executable.endsWith(".jar") ? 
                new ProcessExecution("java", [ "-jar", this.cli.executable,  ...(task.definition.args || [])], processOpts)
                 : new ProcessExecution(this.cli.executable, task.definition.args || [], processOpts);
        }
        return task;
    }
    
}

