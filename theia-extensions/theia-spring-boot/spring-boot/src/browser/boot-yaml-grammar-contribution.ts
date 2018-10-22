import {
    BOOT_PROPERTIES_YAML_LANGUAGE_ID,
    BOOT_PROPERTIES_YAML_LANGUAGE_NAME
} from '../common';
import {LanguageGrammarDefinitionContribution, TextmateRegistry} from '@theia/monaco/lib/browser/textmate';
import {injectable} from 'inversify';
import {YAML_LANGUAGE_GRAMMAR_SCOPE, YAML_CONFIG} from '@pivotal-tools/theia-languageclient/lib/common';


// Boot .yml file language registration

@injectable()
export class BootYamlGrammarContribution implements LanguageGrammarDefinitionContribution {

    registerTextmateLanguage(registry: TextmateRegistry) {
        monaco.languages.register({
            id: BOOT_PROPERTIES_YAML_LANGUAGE_ID,
            aliases: [
                BOOT_PROPERTIES_YAML_LANGUAGE_NAME
            ],
            filenamePatterns: ['application*.yml', 'bootstrap*.yml']
        });

        monaco.languages.setLanguageConfiguration(BOOT_PROPERTIES_YAML_LANGUAGE_ID, YAML_CONFIG);

        registry.mapLanguageIdToTextmateGrammar(BOOT_PROPERTIES_YAML_LANGUAGE_ID, YAML_LANGUAGE_GRAMMAR_SCOPE);
    }
}
