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
import {LanguageGrammarDefinitionContribution, TextmateRegistry} from '@theia/monaco/lib/browser/textmate';
import {injectable} from 'inversify';
import {YAML_LANGUAGE_ID, YAML_LANGUAGE_GRAMMAR_SCOPE, YAML_CONFIG, YAML_TM_GRAMMAR} from '../common';

@injectable()
export class YamlGrammarContribution implements LanguageGrammarDefinitionContribution {

    registerTextmateLanguage(registry: TextmateRegistry) {
        monaco.languages.register({
            id: YAML_LANGUAGE_ID,
            "aliases": [
                "YAML",
                "yaml"
            ],
            "extensions": [
                ".yml",
                ".eyaml",
                ".eyml",
                ".yaml"
            ],
            "filenames": []
        });

        monaco.languages.setLanguageConfiguration(YAML_LANGUAGE_ID, YAML_CONFIG);

        registry.registerTextmateGrammarScope(YAML_LANGUAGE_GRAMMAR_SCOPE, {
            async getGrammarDefinition() {
                return {
                    format: 'json',
                    content: YAML_TM_GRAMMAR
                };
            }
        });

        registry.mapLanguageIdToTextmateGrammar(YAML_LANGUAGE_ID, YAML_LANGUAGE_GRAMMAR_SCOPE);
    }
}