'use strict';

import * as VSCode from 'vscode';
import { workspace } from 'vscode';

import * as commons from '@pivotal-tools/commons-vscode';
import * as liveHoverUi from './live-hover-connect-ui';

import {LanguageClient} from "vscode-languageclient";

const PROPERTIES_LANGUAGE_ID = "spring-boot-properties";
const YAML_LANGUAGE_ID = "spring-boot-properties-yaml";
const JAVA_LANGUAGE_ID = "java";
const XML_LANGUAGE_ID = "xml";

const NEVER_SHOW_AGAIN = "Do not show again";

/** Called when extension is activated */
export function activate(context: VSCode.ExtensionContext): Thenable<LanguageClient> {

    // registerPipelineGenerator(context);
    let options : commons.ActivatorOptions = {
        DEBUG: false,
        CONNECT_TO_LS: false,
        extensionId: 'vscode-spring-boot',
        preferJdk: true,
        checkjvm: (context: VSCode.ExtensionContext, jvm: commons.JVM) => {
            if (!jvm.isJdk()) {
                VSCode.window.showWarningMessage(
                    'JAVA_HOME or PATH environment variable seems to point to a JRE. A JDK is required, hence Boot Hints are unavailable.',
                    NEVER_SHOW_AGAIN).then(selection => {
                        if (selection === NEVER_SHOW_AGAIN) {
                            options.workspaceOptions.update('checkJVM', false);
                        }
                    }
                );
            }
        },
        explodedLsJarData: {
            lsLocation: 'language-server',
            mainClass: 'org.springframework.ide.vscode.boot.app.BootLanguagServerBootApp',
            configFileName: 'application.properties'
        },
        workspaceOptions: VSCode.workspace.getConfiguration("spring-boot.ls"),
        clientOptions: {
            // See PT-158992999 as to why a scheme is added to the document selector
            // documentSelector: [ PROPERTIES_LANGUAGE_ID, YAML_LANGUAGE_ID, JAVA_LANGUAGE_ID ],
            documentSelector: [
                {
                    language: PROPERTIES_LANGUAGE_ID,
                    scheme: 'file'
                },
                {
                    language: YAML_LANGUAGE_ID,
                    scheme: 'file'
                },
                {
                    language: JAVA_LANGUAGE_ID,
                    scheme: 'file'
                },
                {
                    language: JAVA_LANGUAGE_ID,
                    scheme: 'jdt'
                },
                {
                    language: XML_LANGUAGE_ID,
                    scheme: 'file'
                }
            ],
            synchronize: {
                configurationSection: 'boot-java'
            },
            initializationOptions: {
                workspaceFolders: workspace.workspaceFolders ? workspace.workspaceFolders.map(f => f.uri.toString()) : null
            }
        },
        highlightCodeLensSettingKey: 'boot-java.highlight-codelens.on'
    };

    return commons.activate(options, context).then(client => {
        liveHoverUi.activate(client, options, context);
        return client;
    });
}
