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
import { inject, injectable } from 'inversify';
import { Workspace, Languages, LanguageClientFactory } from '@theia/languages/lib/browser';
import { CF_MANIFEST_YAML_LANGUAGE_ID, CF_MANIFEST_YAML_LANGUAGE_NAME } from '../common';
import { StsLanguageClientContribution } from '@pivotal-tools/theia-languageclient/lib/browser/language-client-contribution';

@injectable()
export class CfManifestYamlClientContribution extends StsLanguageClientContribution<null> {

    readonly id = CF_MANIFEST_YAML_LANGUAGE_ID;
    readonly name = CF_MANIFEST_YAML_LANGUAGE_NAME;

    constructor(
        @inject(Workspace) workspace: Workspace,
        @inject(Languages) languages: Languages,
        @inject(LanguageClientFactory) languageClientFactory: LanguageClientFactory,
    ) {
        super(workspace, languages, languageClientFactory);
    }

    protected get globPatterns() {
        return [
            '**/*manifest*.yml'
        ];
    }
}
