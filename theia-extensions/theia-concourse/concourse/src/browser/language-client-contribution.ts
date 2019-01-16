/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
import { injectable, inject } from 'inversify';
import { Workspace, Languages, LanguageClientFactory } from '@theia/languages/lib/browser';
import { StsLanguageClientContribution } from '@pivotal-tools/theia-languageclient/lib/browser/language-client-contribution';
import {
    CONCOURSE_PIPELINE_YAML_LANGUAGE_ID, CONCOURSE_SERVER_ID, CONCOURSE_SERVER_NAME,
    CONCOURSE_TASK_YAML_LANGUAGE_ID
} from '../common';

@injectable()
export class ConcourseClientContribution extends StsLanguageClientContribution<null> {

    readonly id = CONCOURSE_SERVER_ID;
    readonly name = CONCOURSE_SERVER_NAME;

    constructor(
        @inject(Workspace) workspace: Workspace,
        @inject(Languages) languages: Languages,
        @inject(LanguageClientFactory) languageClientFactory: LanguageClientFactory,
    ) {
        super(workspace, languages, languageClientFactory);
    }

    protected get documentSelector() {
        return [CONCOURSE_PIPELINE_YAML_LANGUAGE_ID, CONCOURSE_TASK_YAML_LANGUAGE_ID];
    }

    protected get globPatterns() {
        return [
            '*pipeline*.yml',
            '*task.yml',
            '**/tasks/*.yml'
        ];
    }
}
