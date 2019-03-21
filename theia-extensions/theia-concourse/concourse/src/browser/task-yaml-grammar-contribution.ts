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
import {
    CONCOURSE_TASK_YAML_LANGUAGE_ID,
    CONCOURSE_TASK_YAML_LANGUAGE_NAME
} from '../common';
import {LanguageGrammarDefinitionContribution, TextmateRegistry} from '@theia/monaco/lib/browser/textmate';
import {injectable} from 'inversify';
import {YAML_LANGUAGE_GRAMMAR_SCOPE, YAML_CONFIG} from '@pivotal-tools/theia-languageclient/lib/common';

@injectable()
export class TaskYamlGrammarContribution implements LanguageGrammarDefinitionContribution {

    registerTextmateLanguage(registry: TextmateRegistry) {
        monaco.languages.register({
            id: CONCOURSE_TASK_YAML_LANGUAGE_ID,
            aliases: [
                CONCOURSE_TASK_YAML_LANGUAGE_NAME
            ],
            filenamePatterns: ['*task.yml', '**/tasks/*.yml'],
            firstLine: '^#(\\\\s)*task(\\\\s)*',
        });

        monaco.languages.setLanguageConfiguration(CONCOURSE_TASK_YAML_LANGUAGE_ID, YAML_CONFIG);

        registry.mapLanguageIdToTextmateGrammar(CONCOURSE_TASK_YAML_LANGUAGE_ID, YAML_LANGUAGE_GRAMMAR_SCOPE);
    }
}
