import { BootAddMetadata, BootNewMetadata, Project, ProjectCatalog } from "./cli-types";
import vscode, { TaskScope } from "vscode";

const SPRING_CLI_TASK_TYPE = 'spring-cli';

export class Cli {

    projectList() : Project[] {
        return [
            {
                id: 'web',
                description: 'Hello, World RESTful web service.',
                url: 'https://github.com/rd-1-2022/rest-service',
                catalogId: 'gs',
                tags: ['java-17', 'boot-3.1.x', 'rest', 'web']
            },
            {
                id: 'jpa',
                description: 'Learn how to work with JPA data persistence using Spring Data JPA.',
                url: 'https://github.com/rd-1-2022/rpt-spring-data-jpa',
                catalogId: 'gs',
                tags: ['java-17', 'boot-3.1.x', 'jpa', 'h2']
            },
            {
                id: 'scheduling',
                description: 'How to schedule tasks',
                url: 'https://github.com/rd-1-2022/rpt-spring-scheduling-tasks',
                catalogId: 'gs',
                tags: ['scheduling']
            }
        ];
    }

    projectCatalogList(): Thenable<ProjectCatalog[]> {
        return Promise.resolve([
            {
                name: "gs",
                url: "https://github.com/rd-1-2022/spring-gs-catalog",
                description: "Getting Started Catalog",
                tags: ["java-17", "boot-3.1"]
            },
            {
                name: "ai-azure",
                url: "https://github.com/rd-1-2022/ai-azure-catalog",
                description: "Azure OpenAI Catalog",
                tags: ["java-17", "boot-3.1.x", "ai", "azure"]
            }
        ]);
    }

    projectCatalogListAvailable(): Thenable<ProjectCatalog[]> {
        return Promise.resolve([
            {
                name: "ai-azure",
                url: "https://github.com/rd-1-2022/ai-azure-catalog",
                description: "Azure OpenAI Catalog",
                tags: ["java-17", "boot-3.1.x", "ai", "azure"]
            },
            {
                name: "dapr",
                url: "https://github.com/ciberkleid/spring-cli-dapr-catalog",
                description: "Dapr Catalog",
                tags: ["java-17", "boot-3.1.x", "dapr"]
            }
        ]);
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
        return this.exec("Add Project Catalog", `"${catalog.name}"`, args);
    }

    projectCatalogRemove(name: string): Promise<void> {
        const args = [
            "project-catalog",
            "remove",
            "--name",
            name,
        ];
        return this.exec("Remove Project Catalog", `"${name}"`, args);
    }

    bootNew(metadata: BootNewMetadata): Promise<void> {
        const args = [
            "boot",
            "new",
            "--name",
            `"${metadata.name}"`,
            "--from",
            `"${metadata.catalogId}"`
        ];
        if (metadata.groupId) {
            args.push("--group-id")
            args.push(`"${metadata.groupId}"`);
        }
        if (metadata.artifactId) {
            args.push("--artifact-id")
            args.push(`"${metadata.artifactId}"`);
        }
        if (metadata.rootPackage) {
            args.push("--package-name");
            args.push(`"${metadata.rootPackage}"`);
        }
        return this.exec("New Boot Project", `'${metadata.catalogId}'`, args, metadata.targetFolder);
    }

    bootAdd(metadata: BootAddMetadata): Promise<void> {
        const args = [
            "boot",
            "add",
            "--from",
            metadata.catalogType
        ];
        return this.exec("Add to Boot Project", `'${metadata.catalogType}'`, args, metadata.targetFolder);
    }

    private async exec(title: string, message: string, args: string[], cwd?: string): Promise<void> {

        return vscode.window.withProgress({
            location: vscode.ProgressLocation.Window,
            cancellable: true,
            title
        }, (progress, cancellation) => {
            
            progress.report({message});
        
            return new Promise<void>(async (resolve, reject) => {
                const process = new vscode.ProcessExecution('spring', args, { cwd });
                const task = new vscode.Task({ type: SPRING_CLI_TASK_TYPE}, cwd ? vscode.workspace.getWorkspaceFolder(vscode.Uri.file(cwd)) : TaskScope.Global, `${title}: ${message}`, SPRING_CLI_TASK_TYPE, process);
                const taskExecution = await vscode.tasks.executeTask(task);
                if (cancellation.isCancellationRequested) {
                    reject();
                }
                const cancelListener = cancellation.onCancellationRequested(() => {
                    cancelListener.dispose();
                    reject();
                })
                const listener = vscode.tasks.onDidEndTaskProcess(e => {
                    if (e.execution === taskExecution) {
                        listener.dispose();
                        resolve();
                    }
                });
            });
        
        });
    }

}

