/// <reference types='@typefox/monaco-editor-core/monaco'/>
import {
    BOOT_PROPERTIES_YAML_LANGUAGE_ID,
    BOOT_PROPERTIES_YAML_LANGUAGE_NAME
} from '../common';

// Boot .yml file language registration

let YAML_LANG_MODULE_PROMISE: monaco.Promise<any>;

monaco.languages.register({
    id: BOOT_PROPERTIES_YAML_LANGUAGE_ID,
    filenamePatterns: ['application*.yml', 'bootstrap*.yml'],
    aliases: [BOOT_PROPERTIES_YAML_LANGUAGE_NAME, BOOT_PROPERTIES_YAML_LANGUAGE_ID],
});

monaco.languages.onLanguage(BOOT_PROPERTIES_YAML_LANGUAGE_ID, () => {
    if (!YAML_LANG_MODULE_PROMISE) {
        YAML_LANG_MODULE_PROMISE = (<any>monaco.languages.getLanguages().find(ext => ext.id === 'yaml')).loader();
    }
    return YAML_LANG_MODULE_PROMISE.then(mod => {
        monaco.languages.setLanguageConfiguration(BOOT_PROPERTIES_YAML_LANGUAGE_ID, mod.conf);
        monaco.languages.setMonarchTokensProvider(BOOT_PROPERTIES_YAML_LANGUAGE_ID, mod.language);
    })
});
