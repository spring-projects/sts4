/// <reference types='@typefox/monaco-editor-core/monaco'/>
import {
    BOOT_PROPERTIES_LANGUAGE_ID,
    BOOT_PROPERTIES_LANGUAGE_NAME,
} from '../common';

// Boot .properties file language registration

let PROPERTIES_LANG_MODULE_PROMISE: monaco.Promise<any>;

monaco.languages.register({
    id: BOOT_PROPERTIES_LANGUAGE_ID,
    filenamePatterns: ['application*.properties', 'bootstrap*.properties'],
    aliases: [BOOT_PROPERTIES_LANGUAGE_NAME, BOOT_PROPERTIES_LANGUAGE_ID],
});

monaco.languages.onLanguage(BOOT_PROPERTIES_LANGUAGE_ID, () => {
    if (!PROPERTIES_LANG_MODULE_PROMISE) {
        PROPERTIES_LANG_MODULE_PROMISE = (<any>monaco.languages.getLanguages().find(ext => ext.id === 'ini')).loader();
    }
    return PROPERTIES_LANG_MODULE_PROMISE.then(mod => {
        monaco.languages.setLanguageConfiguration(BOOT_PROPERTIES_LANGUAGE_ID, mod.conf);
        monaco.languages.setMonarchTokensProvider(BOOT_PROPERTIES_LANGUAGE_ID, mod.language);
    })
});

