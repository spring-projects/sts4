import { BootAddMetadata, BootNewMetadata, Project, ProjectCatalog, commandAddMetadata } from "./cli-types";
import { ProcessExecution, ProgressLocation, Task, TaskScope, Uri, env, tasks, window, workspace } from "vscode";
import cp from "child_process";
import { homedir } from "os";
import { getWorkspaceRoot } from "./utils";

const SPRING_CLI_TASK_TYPE = 'spring-cli';

export class Cli {

    private get executable(): string {
        return workspace.getConfiguration("spring-cli").get("executable") || "spring";
    }

    isBorderLine(s: string) {
        return !/(\s|\S)+/.test(s);
    }

    projectList() : Promise<Project[]> {
        return this.fetchJson("Fetching projects...", undefined, ["project", "list"]);
    }

    projectCatalogList(): Promise<ProjectCatalog[]> {
        return this.fetchJson("Fetching Catalogs...", undefined, ["project-catalog", "list"]);
    }

    projectCatalogListAvailable(): Promise<ProjectCatalog[]> {
        return this.fetchJson("Fetching Available Catalogs...", undefined, ["project-catalog", "list-available"]);
    }

    commandAdd(metadata: commandAddMetadata) {
        const args = [
            "command",
            "add",
            "--from",
            metadata.url
        ];
        this.exec("Add Command", metadata.url, args);
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
                const process = this.executable.endsWith(".jar") ? new ProcessExecution("java", [ "-jar", this.executable,  ...args], processOpts) : new ProcessExecution(this.executable, args, processOpts);
                const task = new Task({ type: SPRING_CLI_TASK_TYPE }, cwd ? workspace.getWorkspaceFolder(Uri.file(cwd)) : TaskScope.Global, `${title}: ${message}`, SPRING_CLI_TASK_TYPE, process);
                const taskExecution = await tasks.executeTask(task);
                if (cancellation.isCancellationRequested) {
                    reject();
                }
                const cancelListener = cancellation.onCancellationRequested(() => {
                    cancelListener.dispose();
                    reject();
                })
                const listener = tasks.onDidEndTaskProcess(e => {
                    if (e.execution === taskExecution) {
                        listener.dispose();
                        resolve();
                    }
                });
            });
        
        });
    }

    private async fetchJson<T>(title: string, message: string, args: string[], cwd?: string) : Promise<T> {

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
                    reject("Cancelled");
                }
                const processOpts = { cwd: cwd || getWorkspaceRoot()?.fsPath || homedir() };
                const process = this.executable.endsWith(".jar") ? await cp.exec(`java -jar ${this.executable} ${args.join(" ")} --json`, processOpts) : await cp.exec(`${this.executable} ${args.join(" ")}`, processOpts);
                cancellation.onCancellationRequested(() => process.kill());
                const dataChunks: string[] = [];
                process.stdout.on("data", s => dataChunks.push(s));
                process.on("exit", (code) => {
                    if (code) {
                        reject(`Failed to fetch data: ${dataChunks.join()}`);
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

