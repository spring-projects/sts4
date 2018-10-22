import {
    BOOT_PROPERTIES_LANGUAGE_ID,
    BOOT_PROPERTIES_LANGUAGE_NAME,
} from '../common';
import {LanguageGrammarDefinitionContribution, TextmateRegistry} from '@theia/monaco/lib/browser/textmate';
import {injectable} from 'inversify';
import {
    JAVA_PROPERTIES_LANGUAGE_GRAMMAR_SCOPE,
    JAVA_PROPERTIES_CONFIG
} from '@pivotal-tools/theia-languageclient/lib/common';


// Boot .properties file language registration

@injectable()
export class BootPropertiesGrammarContribution implements LanguageGrammarDefinitionContribution {

    registerTextmateLanguage(registry: TextmateRegistry) {
        monaco.languages.register({
            id: BOOT_PROPERTIES_LANGUAGE_ID,
            aliases: [
                BOOT_PROPERTIES_LANGUAGE_NAME
            ],
            filenamePatterns: ['application*.properties', 'bootstrap*.properties']
        });

        monaco.languages.setLanguageConfiguration(BOOT_PROPERTIES_LANGUAGE_ID, JAVA_PROPERTIES_CONFIG);
        registry.mapLanguageIdToTextmateGrammar(BOOT_PROPERTIES_LANGUAGE_ID, JAVA_PROPERTIES_LANGUAGE_GRAMMAR_SCOPE);
    }
}
