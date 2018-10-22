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