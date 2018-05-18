/// <reference types='monaco-editor-core/monaco'/>
import {
    BOSH_DEPLOYMENT_YAML_LANGUAGE_ID,
    BOSH_DEPLOYMENT_YAML_LANGUAGE_NAME
} from '../common';

// Deployment .yml file language registration

let YAML_LANG_MODULE_PROMISE: monaco.Promise<any>;

monaco.languages.register({
    id: BOSH_DEPLOYMENT_YAML_LANGUAGE_ID,
    filenamePatterns: ['*deployment*.yml'],
    aliases: [BOSH_DEPLOYMENT_YAML_LANGUAGE_NAME]
});

monaco.languages.onLanguage(BOSH_DEPLOYMENT_YAML_LANGUAGE_ID, () => {
    if (!YAML_LANG_MODULE_PROMISE) {
        YAML_LANG_MODULE_PROMISE = (<any>monaco.languages.getLanguages().find(ext => ext.id === 'yaml')).loader();
    }
    return YAML_LANG_MODULE_PROMISE.then(mod => {
        monaco.languages.setLanguageConfiguration(BOSH_DEPLOYMENT_YAML_LANGUAGE_ID, mod.conf);
        monaco.languages.setMonarchTokensProvider(BOSH_DEPLOYMENT_YAML_LANGUAGE_ID, mod.language);
    })
});
