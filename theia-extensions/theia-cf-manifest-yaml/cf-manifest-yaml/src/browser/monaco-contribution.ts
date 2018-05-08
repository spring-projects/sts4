/// <reference types='monaco-editor-core/monaco'/>
import {
    CF_MANIFEST_YAML_LANGUAGE_ID,
    CF_MANIFEST_YAML_LANGUAGE_NAME
} from '../common';

let YAML_LANG_MODULE_PROMISE: monaco.Promise<any>;

monaco.languages.register({
    id: CF_MANIFEST_YAML_LANGUAGE_ID,
    filenamePatterns: ['*manifest*.yml'],
    aliases: [CF_MANIFEST_YAML_LANGUAGE_NAME],
});

monaco.languages.onLanguage(CF_MANIFEST_YAML_LANGUAGE_ID, () => {
    if (!YAML_LANG_MODULE_PROMISE) {
        YAML_LANG_MODULE_PROMISE = (<any>monaco.languages.getLanguages().find(ext => ext.id === 'yaml')).loader();
    }
    return YAML_LANG_MODULE_PROMISE.then(mod => {
        monaco.languages.setLanguageConfiguration(CF_MANIFEST_YAML_LANGUAGE_ID, mod.conf);
        monaco.languages.setMonarchTokensProvider(CF_MANIFEST_YAML_LANGUAGE_ID, mod.language);
    })
});