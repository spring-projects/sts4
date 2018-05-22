/// <reference types='monaco-editor-core/monaco'/>
import {
    CONCOURSE_PIPELINE_YAML_LANGUAGE_ID,
    CONCOURSE_PIPELINE_YAML_LANGUAGE_NAME
} from '../common';

// Pipeline .yml file language registration

let YAML_LANG_MODULE_PROMISE: monaco.Promise<any>;

monaco.languages.register({
    id: CONCOURSE_PIPELINE_YAML_LANGUAGE_ID,
    filenamePatterns: ['*pipeline*.yml'],
    aliases: [CONCOURSE_PIPELINE_YAML_LANGUAGE_NAME, CONCOURSE_PIPELINE_YAML_LANGUAGE_ID],
    firstLine: '^#(\\s)*pipeline(\\s)*',
});

monaco.languages.onLanguage(CONCOURSE_PIPELINE_YAML_LANGUAGE_ID, () => {
    if (!YAML_LANG_MODULE_PROMISE) {
        YAML_LANG_MODULE_PROMISE = (<any>monaco.languages.getLanguages().find(ext => ext.id === 'yaml')).loader();
    }
    return YAML_LANG_MODULE_PROMISE.then(mod => {
        monaco.languages.setLanguageConfiguration(CONCOURSE_PIPELINE_YAML_LANGUAGE_ID, mod.conf);
        monaco.languages.setMonarchTokensProvider(CONCOURSE_PIPELINE_YAML_LANGUAGE_ID, mod.language);
    })
});
