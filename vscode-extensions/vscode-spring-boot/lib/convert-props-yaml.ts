import * as path from "path";
import { existsSync } from "fs";
import { ExtensionContext, Uri, commands, window, workspace } from "vscode";

export function startPropertiesConversionSupport(extension: ExtensionContext) {
    extension.subscriptions.push(
        commands.registerCommand('vscode-spring-boot.props-to-yaml', async (uri) => {

            if (!uri && window.activeTextEditor) {
                const activeUri = window.activeTextEditor.document.uri;
                if (".properties" === path.extname(activeUri.path)) {
                    uri = activeUri;
                }
            }
    
            if (!uri) {
                throw new Error("No '.properties' file selected");
            }
    
            return await commands.executeCommand("sts/boot/props-to-yaml", uri.toString(), Uri.file(getTargetFile(uri.path, "yml")).toString(), shouldReplace());
        }),

        commands.registerCommand('vscode-spring-boot.yaml-to-props', async (uri) => {

            if (!uri && window.activeTextEditor) {
                const activeUri = window.activeTextEditor.document.uri;
                const ext = path.extname(activeUri.path)
                if (".yml" === ext || ".yaml" === ext) {
                    uri = activeUri;
                }
            }
    
            if (!uri) {
                throw new Error("No '.yaml' file selected");
            }

            return await commands.executeCommand("sts/boot/yaml-to-props", uri.toString(), Uri.file(getTargetFile(uri.path, "properties")).toString(), shouldReplace());
        })
    );
}

function getTargetFile(sourcePath: string, ext: string): string {
    const dir = path.dirname(sourcePath);
    const fileName = path.basename(sourcePath);
    const filenameNoExt = path.basename(sourcePath).substring(0, fileName.length - path.extname(fileName).length);
    let targetPath = path.join(dir, `${filenameNoExt}.${ext}`);
    for (let i = 1; i < Number.MAX_SAFE_INTEGER && existsSync(targetPath); i++) {
        targetPath = path.join(dir, `${filenameNoExt}-${i}.${ext}`)
    }
    return targetPath;
}

function shouldReplace(): boolean {
    return workspace.getConfiguration("spring.tools.properties").get("replace-converted-file");
}

