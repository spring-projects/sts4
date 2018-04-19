/// <reference types='monaco-editor-core/monaco'/>
import { CF_MANIFEST_YAML_LANGUAGE_ID, CF_MANIFEST_YAML_LANGUAGE_NAME } from '../../common';
import { conf, language } from "./yaml";

monaco.languages.register({
    id: CF_MANIFEST_YAML_LANGUAGE_ID,
    filenamePatterns: ['*manifest*.yml'],
    aliases: [CF_MANIFEST_YAML_LANGUAGE_NAME],
});

monaco.languages.onLanguage(CF_MANIFEST_YAML_LANGUAGE_ID, () => {
    monaco.languages.setLanguageConfiguration(CF_MANIFEST_YAML_LANGUAGE_ID, conf);
    monaco.languages.setMonarchTokensProvider(CF_MANIFEST_YAML_LANGUAGE_ID, language);
});