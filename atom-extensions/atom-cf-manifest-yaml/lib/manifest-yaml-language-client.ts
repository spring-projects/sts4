import * as path from 'path';
import { JavaProcessLanguageClient } from '@pivotal-tools/atom-languageclient-commons';
import {JVM} from '@pivotal-tools/jvm-launch-utils';

export class ManifestYamlLanguageClient extends JavaProcessLanguageClient {

    constructor() {
        //noinspection JSAnnotator
        super(
            path.join(__dirname, '..', 'server'),
            'cf-manifest-language-server.jar'
        );
    }

    getGrammarScopes() {
        return ['source.cf-manifest-yaml']
    }

    getLanguageName() {
        return 'Manifest-YAML'
    }

    getServerName() {
        return 'CF Manifest YAML'
    }

    activate() {
        // replace the example argument 'linter-ruby' with the name of this Atom package
        require('atom-package-deps')
            .install('cf-manifest-yaml')
            .then(() => console.debug('All dependencies installed, good to go'));
        super.activate();
    }

    launchVmArgs(jvm: JVM) {
        return Promise.resolve([
            '-Dorg.slf4j.simpleLogger.logFile=manifest-yaml.log',
            '-Dorg.slf4j.simpleLogger.defaultLogLevel=debug',
        ]);

    }

}