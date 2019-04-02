import * as path from 'path';
import { JavaProcessLanguageClient, JavaOptions } from '@pivotal-tools/atom-languageclient-commons';
import {JVM} from '@pivotal-tools/jvm-launch-utils';

export class ManifestYamlLanguageClient extends JavaProcessLanguageClient {

    constructor() {
        //noinspection JSAnnotator
        super(
            path.join(__dirname, '..', 'server'),
            'cf-manifest-language-server.jar'
        );
        // this.DEBUG = true;
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
            // '-Xdebug',
            // '-agentlib:jdwp=transport=dt_socket,server=y,address=7999,suspend=n',
            '-Dorg.slf4j.simpleLogger.logFile=manifest-yaml.log',
        ]);

    }

    getJavaOptions(): JavaOptions {
        const home = atom.config.get('cf-manifest-yaml.java.home');
        const vmargs = atom.config.get('cf-manifest-yaml.java.vmargs');
        return {
            home: typeof home === 'string' ? home : undefined,
            vmargs: Array.isArray(vmargs) ? vmargs :  undefined
        };
    }

}