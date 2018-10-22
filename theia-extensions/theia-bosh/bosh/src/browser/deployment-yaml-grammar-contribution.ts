import {
    BOSH_DEPLOYMENT_YAML_LANGUAGE_ID,
    BOSH_DEPLOYMENT_YAML_LANGUAGE_NAME
} from '../common';
import {LanguageGrammarDefinitionContribution, TextmateRegistry} from '@theia/monaco/lib/browser/textmate';
import {injectable} from 'inversify';
import {YAML_LANGUAGE_GRAMMAR_SCOPE, YAML_CONFIG} from '@pivotal-tools/theia-languageclient/lib/common';

@injectable()
export class DeploymentYamlGrammarContribution implements LanguageGrammarDefinitionContribution {

    registerTextmateLanguage(registry: TextmateRegistry) {
        monaco.languages.register({
            id: BOSH_DEPLOYMENT_YAML_LANGUAGE_ID,
            aliases: [ BOSH_DEPLOYMENT_YAML_LANGUAGE_NAME ],
            filenamePatterns: [ '*deployment*.yml' ]
        });

        monaco.languages.setLanguageConfiguration(BOSH_DEPLOYMENT_YAML_LANGUAGE_ID, YAML_CONFIG);

        registry.mapLanguageIdToTextmateGrammar(BOSH_DEPLOYMENT_YAML_LANGUAGE_ID, YAML_LANGUAGE_GRAMMAR_SCOPE);
    }
}
