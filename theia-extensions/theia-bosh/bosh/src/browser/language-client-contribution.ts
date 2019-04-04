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
import { injectable, inject } from 'inversify';
import { Workspace, Languages, LanguageClientFactory } from '@theia/languages/lib/browser';
import { StsLanguageClientContribution } from '@pivotal-tools/theia-languageclient/lib/browser/language-client-contribution';
import { JavaLsProcessParameters } from '@pivotal-tools/theia-languageclient/lib//common';
import {
    BOSH_DEPLOYMENT_YAML_LANGUAGE_ID,
    BOSH_CLOUDCONFIG_YAML_LANGUAGE_ID,
    BOSH_SERVER_ID,
    BOSH_SERVER_NAME
} from '../common';
import { BoshConfiguration, BoshPreferences } from './bosh-preferences';

@injectable()
export class BoshClientContribution extends StsLanguageClientContribution<BoshConfiguration> {

    readonly id = BOSH_SERVER_ID;
    readonly name = BOSH_SERVER_NAME;

    constructor(
        @inject(Workspace) workspace: Workspace,
        @inject(Languages) languages: Languages,
        @inject(LanguageClientFactory) languageClientFactory: LanguageClientFactory,
        @inject(BoshPreferences) protected readonly preferences: BoshPreferences
    ) {
        super(workspace, languages, languageClientFactory);
    }

    protected get documentSelector() {
        return [BOSH_DEPLOYMENT_YAML_LANGUAGE_ID, BOSH_CLOUDCONFIG_YAML_LANGUAGE_ID];
    }

    protected get globPatterns() {
        return [
            '*deployment*.yml',
            '*cloud-config*.yml'
        ];
    }

    protected getStartParameters(): JavaLsProcessParameters {
        return {
            javahome: this.preferences['bosh-yaml.ls.javahome'],
            vmargs: this.preferences['bosh-yaml.ls.vmargs']
        };
    }

}
