/// <reference types='monaco-editor-core/monaco'/>
import {
    BOSH_CLOUDCONFIG_YAML_LANGUAGE_ID,
    BOSH_CLOUDCONFIG__YAML_LANGUAGE_NAME
} from '../common';

// Task .yml file language registration

let YAML_LANG_MODULE_PROMISE: monaco.Promise<any>;

monaco.languages.register({
    id: BOSH_CLOUDCONFIG_YAML_LANGUAGE_ID,
    filenamePatterns: ['*cloud-config*.yml'],
    aliases: [BOSH_CLOUDCONFIG__YAML_LANGUAGE_NAME]
});

monaco.languages.onLanguage(BOSH_CLOUDCONFIG_YAML_LANGUAGE_ID, () => {
    if (!YAML_LANG_MODULE_PROMISE) {
        YAML_LANG_MODULE_PROMISE = (<any>monaco.languages.getLanguages().find(ext => ext.id === 'yaml')).loader();
    }
    return YAML_LANG_MODULE_PROMISE.then(mod => {
        monaco.languages.setLanguageConfiguration(BOSH_CLOUDCONFIG_YAML_LANGUAGE_ID, mod.conf);
        monaco.languages.setMonarchTokensProvider(BOSH_CLOUDCONFIG_YAML_LANGUAGE_ID, mod.language);
    })
});
