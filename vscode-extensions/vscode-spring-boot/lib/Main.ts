'use strict';

import * as OS from "os";
import * as VSCode from 'vscode';
import { workspace } from 'vscode';

import * as commons from '@pivotal-tools/commons-vscode';
import * as liveHoverUi from './live-hover-connect-ui';
import * as rewrite from './rewrite';

import { startDebugSupport } from './debug-config-provider';
import { ApiManager } from "./apiManager";
import { ExtensionAPI } from "./api";

const PROPERTIES_LANGUAGE_ID = "spring-boot-properties";
const YAML_LANGUAGE_ID = "spring-boot-properties-yaml";
const JAVA_LANGUAGE_ID = "java";
const XML_LANGUAGE_ID = "xml";
const FACTORIES_LANGUAGE_ID = "spring-factories";

const YES = 'Yes';
const NO = 'No';
const NEVER_SHOW_AGAIN = "Do not show again";
const RECONCILING_PREF_KEY = 'boot-java.rewrite.reconcile';
const RECONCILING_PROMPT_PREF_KEY = 'vscode-spring-boot.rewrite.reconcile-prompt';

/** Called when extension is activated */
export function activate(context: VSCode.ExtensionContext): Thenable<ExtensionAPI> {

    // registerPipelineGenerator(context);
    let options : commons.ActivatorOptions = {
        DEBUG: false,
        CONNECT_TO_LS: false,
        extensionId: 'vscode-spring-boot',
        preferJdk: true,
        checkjvm: (context: VSCode.ExtensionContext, jvm: commons.JVM) => {
            let version = jvm.getMajorVersion();
            if (version < 17) {
                throw Error(`Spring Tools Language Server requires Java 17 or higher to be launched. Current Java version is ${version}`);
            }

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
            mainClass: 'org.springframework.ide.vscode.boot.app.BootLanguageServerBootApp',
            configFileName: 'application.properties'
        },
        workspaceOptions: VSCode.workspace.getConfiguration("spring-boot.ls"),
        clientOptions: {
            uriConverters: {
                code2Protocol: (uri) => {
           		        			/*
                    * Workaround for docUri coming from vscode-languageclient on Windows
                    * 
                    * It comes in as "file:///c%3A/Users/ab/spring-petclinic/src/main/java/org/springframework/samples/petclinic/owner/PetRepository.java"
                    * 
                    * While symbols index would have this uri instead:
                    * - "file:///C:/Users/ab/spring-petclinic/src/main/java/org/springframework/samples/petclinic/owner/PetRepository.java"
                    * 
                    * i.e. lower vs upper case drive letter and escaped drive colon  
                    */
                    if (OS.platform() === "win32" && uri.scheme === 'file') {
                        let uriStr = uri.toString(true);
                        const idx = uriStr.indexOf(':', 5);
                        if (idx > 5 && idx < 10) {
                            uriStr = `${uriStr.substring(0, idx - 1)}${uriStr.charAt(idx - 1).toUpperCase()}${uriStr.substring(idx)}`
                        }
                        return uriStr;
                    }
                    return uri.toString();
                },
                protocol2Code: uri => VSCode.Uri.parse(uri)
            },
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
                },
                {
                    language: FACTORIES_LANGUAGE_ID,
                    scheme: 'file'
                }
            ],
            synchronize: {
                configurationSection: ['boot-java', 'spring-boot']
            },
            initializationOptions: () => ({
                workspaceFolders: workspace.workspaceFolders ? workspace.workspaceFolders.map(f => f.uri.toString()) : null,
                enableJdtClasspath: true
            })
        },
        highlightCodeLensSettingKey: 'boot-java.highlight-codelens.on'
    };

    // Register launch config contributior to java debug launch to be able to connect to JMX
    context.subscriptions.push(startDebugSupport());

    return commons.activate(options, context).then(client => {
        VSCode.commands.registerCommand('vscode-spring-boot.ls.start', () => client.start().then(() => {
            // Boot LS is fully started

            // Force classpath listener to be enabled. Boot LS can only be launched iff classpath is available and there Spring-Boot on the classpath somewhere.
            VSCode.commands.executeCommand('sts.vscode-spring-boot.enableClasspathListening', true);

            // Ask user to enable Boot java source reconciling feature if disabled
            if (VSCode.workspace.getConfiguration().get(RECONCILING_PROMPT_PREF_KEY) && !VSCode.workspace.getConfiguration().get(RECONCILING_PREF_KEY)) {
                VSCode.window.showInformationMessage('Do you wish to enable additional Java sources reconciling to get Spring specific validations and suggestions?', YES, NO, NEVER_SHOW_AGAIN).then(answer => {
                    switch (answer) {
                        case YES:
                            VSCode.workspace.getConfiguration().update(RECONCILING_PREF_KEY, true, true);
                            break;
                        case NEVER_SHOW_AGAIN:
                            VSCode.workspace.getConfiguration().update(RECONCILING_PROMPT_PREF_KEY, false, true);
                            break;
                        default:
                            break;   
                    }
                });
            }
        }));
        VSCode.commands.registerCommand('vscode-spring-boot.ls.stop', () => client.stop());
        liveHoverUi.activate(client, options, context);
        rewrite.activate(client, options, context);
        return new ApiManager(client).api;
    });
}
