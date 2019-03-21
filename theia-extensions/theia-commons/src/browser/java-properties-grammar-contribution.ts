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
    JAVA_PROPERTIES_LANGUAGE_ID,
    JAVA_PROPERTIES_LANGUAGE_GRAMMAR_SCOPE,
    JAVA_PROPERTIES_CONFIG,
    JAVA_PROPERTIES_TM_GRAMMAR
} from '../common';
import {LanguageGrammarDefinitionContribution, TextmateRegistry} from '@theia/monaco/lib/browser/textmate';
import {injectable} from 'inversify';

// Java .properties file language registration

@injectable()
export class JavaPropertiesGrammarContribution implements LanguageGrammarDefinitionContribution {

    registerTextmateLanguage(registry: TextmateRegistry) {
        monaco.languages.register({
            id: JAVA_PROPERTIES_LANGUAGE_ID,
            aliases: [
                "Java Properties"
            ],
            extensions: [
                ".properties"
            ],
            filenames: []
        });

        monaco.languages.setLanguageConfiguration(JAVA_PROPERTIES_LANGUAGE_ID, JAVA_PROPERTIES_CONFIG);

        registry.registerTextmateGrammarScope(JAVA_PROPERTIES_LANGUAGE_GRAMMAR_SCOPE, {
            async getGrammarDefinition() {
                return {
                    format: 'json',
                    content: JAVA_PROPERTIES_TM_GRAMMAR
                };
            }
        });

        registry.mapLanguageIdToTextmateGrammar(JAVA_PROPERTIES_LANGUAGE_ID, JAVA_PROPERTIES_LANGUAGE_GRAMMAR_SCOPE);
    }
}
