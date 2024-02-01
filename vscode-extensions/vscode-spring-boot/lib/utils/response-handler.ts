import * as vscode from 'vscode';
import { ClassNameExtractor } from './ClassNameExtractor';
const fs = require('fs');

export function extractCodeBlocks(response) {
    const regexJava = /```java\n([\s\S]*?)\n```/g;
    const regexXML = /```xml\n([\s\S]*?)\n```/g;

    const javaCodeBlocks = [];
    const xmlCodeBlocks = [];

    let match;

    // Extract Java code blocks
    while ((match = regexJava.exec(response)) !== null) {
        javaCodeBlocks.push(match[1]);
    }

    // Extract XML code blocks
    while ((match = regexXML.exec(response)) !== null) {
        xmlCodeBlocks.push(match[1]);
    }

    processJavaCode(javaCodeBlocks);
    createXMLFileWithCopilotCode(xmlCodeBlocks[0]);
    return { javaCodeBlocks, xmlCodeBlocks };
}

async function isFileWithNameExists(fileName: string): Promise<boolean> {
    const files = await vscode.workspace.findFiles(`**/${fileName}`, '**/node_modules/**', 1);
    return files.length > 0;
}


async function processJavaCode(javaCodeBlocks) {
    for (let i = 0; i < javaCodeBlocks.length; i++) {
        const classNameExtractor = new ClassNameExtractor();
        const extractFileName = classNameExtractor.extractFileNames(javaCodeBlocks[i]);
        const className = extractFileName != null ? extractFileName : `sample-${i}`;
        createJavaFilesWithCopilotCode(className, javaCodeBlocks, i);      
    }
}

function createJavaFilesWithCopilotCode(className: string, javaCodeBlocks, i: number) {
    isFileWithNameExists(`${className}.java`).then(async (exists) => {
        if (exists) {
            vscode.window.showInformationMessage(`A file with the name '${className}' exists in the project.`);
        } else {
            const rootPath = vscode.workspace.workspaceFolders[0].uri.fsPath;
            const filePath = vscode.Uri.parse(`${rootPath}/${className}.java`);
            try {
                await vscode.workspace.fs.writeFile(filePath, new TextEncoder().encode(""));
                console.log(`File created successfully at: ${filePath}`);
            } catch (error) {
                console.error(`Error creating file at ${filePath}: ${error}`);
            }
            if (fs.existsSync(filePath.fsPath)) {       
            const doc = await vscode.workspace.openTextDocument(filePath);
            const editor = await vscode.window.showTextDocument(doc);

            editor.edit((editBuilder) => {
                const firstLine = new vscode.Position(0, 0);
                editBuilder.insert(firstLine, javaCodeBlocks[i]);
            });

            await doc.save();

            vscode.window.showInformationMessage(`Java file ${className} with Copilot code created successfully!`);
            } else {
                vscode.window.showWarningMessage(`Java file ${className} doesnt exists.`);
            }
        }
    });
}

async function findDependenciesTag(filePath: vscode.Uri) {
    const document = vscode.window.activeTextEditor.document;

    try {
        // const uri = vscode.Uri.file(filePath);
        const document = await vscode.workspace.openTextDocument(filePath);
        for (let i = 0; i < document.lineCount; i++) {
            const line = document.lineAt(i);
            if (line.text.includes('</dependencies>')) {
                return new vscode.Position(i - 1, 0);
            }
        }
        // await vscode.window.showTextDocument(document);
    } catch (error) {
        vscode.window.showErrorMessage(`Error opening file: ${error.message}`);
    }
    return null;
}

async function appendDependency(filePath: vscode.Uri, codeBlock: string) {
    const position = await findDependenciesTag(filePath);

    if (position) {
        const edit = new vscode.WorkspaceEdit();
        edit.insert(filePath, position, codeBlock);

        vscode.workspace.applyEdit(edit);
        // await vscode.commands.executeCommand('editor.action.showRefactorPreview');
    } else {
        console.log('The <dependencies> tag was not found in the document');
    }
}

async function createXMLFileWithCopilotCode(xmlCodeBlock) {
    const rootPath = vscode.workspace.workspaceFolders[0].uri.fsPath;
    const filePath = vscode.Uri.parse(`${rootPath}/pom.xml`);
    appendDependency(filePath, xmlCodeBlock);
    // try {
    //     await vscode.workspace.fs.writeFile(filePath, new TextEncoder().encode(""));
    //     console.log(`File created successfully at: ${filePath}`);
    // } catch (error) {
    //     console.error(`Error creating file at ${filePath}: ${error}`);
    // }

    // if (fs.existsSync(filePath.fsPath)) {      
    // const doc = await vscode.workspace.openTextDocument(filePath);
    // const editor = await vscode.window.showTextDocument(doc);

    // editor.edit((editBuilder) => {
    //     const firstLine = new vscode.Position(0, 0);
    //     editBuilder.insert(firstLine, xmlCodeBlock);
    // });

    // await doc.save();

    // vscode.window.showInformationMessage(`XML file with Copilot code created successfully!`);
    // } else {
    //     vscode.window.showWarningMessage(`Pom file doesnt exists. Skipped creation.`);
    // }
}

