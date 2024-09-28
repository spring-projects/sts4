// import { ProgressLocation, Uri, window, workspace } from "vscode";
// import cp from "child_process";
// import * as vscode from 'vscode';
// import { homedir } from "os";
// import { getWorkspaceRoot, getWorkspaceRootPath } from "./util";
// import path from "path";
// import { WorkspaceEdit } from "vscode-languageclient";

// export const CANCELLED = "Cancelled";

// export class SpringCli {

//     get executable(): string {
//         return workspace.getConfiguration("spring-cli").get("executable") || "spring";
//     }

//     guideLspEdit(uri: Uri, cwd?: string): Promise<WorkspaceEdit> {
//         const args = [
//             "guide",
//             "apply",
//             "--lsp-edit",
//             "--file",
//             uri.fsPath
//         ];
//         return this.fetchJson("Applying guide", uri.fsPath, args, cwd || path.dirname(uri.fsPath), true);
//     }

//     enhanceResponse(uri: Uri, cwd: string): Thenable<string> {
//         const args = [
//             "ai",
//             "enhance-response",
//             "--file",
//             uri.fsPath
//         ];
//         return this.exec("Spring cli ai", "Enhance response", args, cwd);
//     }

//     private async executeCommand(args: string[], cwd?: string): Promise<string> {
//         const processOpts = { cwd: cwd || (await getWorkspaceRoot())?.fsPath || homedir() };
//         const process = this.executable.endsWith(".jar") ? await cp.exec(`java -jar ${this.executable} ${args.join(" ")}`, processOpts) : await cp.exec(`${this.executable} ${args.join(" ")}`, processOpts);
//         const dataChunks: string[] = [];
//         process.stdout.on("data", s => dataChunks.push(s));
//         return new Promise<string>((resolve, reject) => {
//             process.on("exit", (code) => {
//                 if (code) {
//                     reject(`Failed to execute command: ${dataChunks.join()}`);
//                 } else {
//                     resolve(dataChunks.join());
//                 }
//             });
//         });
//     }

//     private async exec<T>(title: string, message: string, args: string[], cwd?: string): Promise<T> {
//         return vscode.window.withProgress({
//             location: vscode.ProgressLocation.Window,
//             cancellable: true,
//             title,
//         }, async (progress, cancellation) => {

//             if (message) {
//                 progress.report({ message });
//             }

//             return new Promise<T>(async (resolve, reject) => {
//                 if (cancellation.isCancellationRequested) {
//                     reject("Cancelled");
//                 }
//                 try {
//                     const output: string = await this.executeCommand(args, cwd);
//                     resolve(output as T);
//                 } catch (error) {
//                     console.error(`Error: ${error}`);
//                     reject(error);
//                 }
//             });
//         });
//     }

//     private async fetchJson<T>(title: string, message: string, args: string[], cwd?: string, omitJsonParam?: boolean): Promise<T> {

//         return window.withProgress({
//             location: ProgressLocation.Window,
//             cancellable: true,
//             title
//         }, (progress, cancellation) => {

//             if (message) {
//                 progress.report({ message });
//             }

//             return new Promise<T>(async (resolve, reject) => {
//                 if (cancellation.isCancellationRequested) {
//                     reject(CANCELLED);
//                 }
//                 const processOpts = { cwd: cwd || getWorkspaceRootPath()?.fsPath || homedir() };
//                 const process = this.executable.endsWith(".jar") ? await cp.exec(`java -jar ${this.executable} ${args.join(" ")}`, processOpts) : await cp.exec(`${this.executable} ${args.join(" ")} ${omitJsonParam ? "" : "--json"}`, processOpts);
//                 cancellation.onCancellationRequested(() => process.kill());
//                 const dataChunks: string[] = [];
//                 process.stdout.on("data", s => dataChunks.push(s));
//                 process.on("exit", (code) => {
//                     if (code) {
//                         if (cancellation.isCancellationRequested) {
//                             reject(CANCELLED);
//                         } else {
//                             reject(`Failed to fetch data: ${dataChunks.join()}`);
//                         }
//                     } else {
//                         try {
//                             resolve(JSON.parse(dataChunks.join()) as T);
//                         } catch (error) {
//                             reject(error);
//                         }
//                     }
//                 });
//             });
//         });
//     }

// }

