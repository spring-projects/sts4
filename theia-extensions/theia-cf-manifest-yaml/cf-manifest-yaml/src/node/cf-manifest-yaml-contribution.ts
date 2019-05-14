/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
import * as path from 'path';
import { injectable } from 'inversify';
import { CF_MANIFEST_YAML_LANGUAGE_ID, CF_MANIFEST_YAML_LANGUAGE_NAME } from '../common';
import { StsLanguageServerContribution } from "@pivotal-tools/theia-languageclient/lib/node/language-server-contribution";

@injectable()
export class CfManifestYamlContribution extends StsLanguageServerContribution {

    readonly id = CF_MANIFEST_YAML_LANGUAGE_ID;
    readonly name = CF_MANIFEST_YAML_LANGUAGE_NAME;
    protected readonly lsLocation = path.resolve(__dirname, '../../server/cf-manifest-yaml-language-server');
    protected readonly configFileName = 'application.properties';
    protected readonly mainClass = 'org.springframework.ide.vscode.manifest.yaml.ManifestYamlLanguageServerBootApp';
    protected readonly jvmArguments = [
        // '-Xdebug',
        // '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=7999',
        // '-Dlog.level=ALL',
        '-Dorg.slf4j.simpleLogger.logFile=cf-manifest-yaml.log'
    ];

}