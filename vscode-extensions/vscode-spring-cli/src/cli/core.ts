import { BootNewProjectMetadata, ProjectType } from "./types";
import vscode from "vscode";

const SPRING_CLI_TASK_TYPE = 'spring-cli';

export class Cli {

    getProjectTypes() : ProjectType[] {
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

    createBootProject(metadata: BootNewProjectMetadata): Promise<void> {
        const args = [
            "boot",
            "new",
            "--name",
            `"${metadata.name}"`,
            "--from",
            `"${metadata.catalogType}"`
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
        return this.exec('Create Spring Boot project', `'${metadata.catalogType}'`, metadata.targetFolder, args);
    }

    private async exec(title: string, message: string, cwd: string, args: string[]): Promise<void> {

        return vscode.window.withProgress({
            location: vscode.ProgressLocation.Window,
            cancellable: true,
            title
        }, (progress, cancellation) => {
            
            progress.report({message});
        
            return new Promise<void>(async (resolve, reject) => {
                const process = new vscode.ProcessExecution('spring', args, { cwd });
                const task = new vscode.Task({ type: SPRING_CLI_TASK_TYPE}, vscode.workspace.getWorkspaceFolder(vscode.Uri.file(cwd)), `${title}: ${message}`, SPRING_CLI_TASK_TYPE, process);
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

