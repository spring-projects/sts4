import * as path from 'path';
import {JavaProcessLanguageClient} from '@pivotal-tools/atom-languageclient-commons';
import {ActiveServer} from 'atom-languageclient';
import {JVM} from '@pivotal-tools/jvm-launch-utils';


export class BoshYamlClient extends JavaProcessLanguageClient {

    constructor() {
        //noinspection JSAnnotator
        super(
            path.join(__dirname, '..', 'server'),
            'bosh-language-server.jar'
        );
    }

    postInitialization(server: ActiveServer) {
        this.sendConfig(server);
        (<any>this)._disposable.add(atom.config.observe('bosh-yaml', () => this.sendConfig(server)));
    }

    getGrammarScopes() {
        return ['source.bosh-deployment-manifest', 'source.bosh-cloud-config'];
    }

    getLanguageName() {
        return 'Bosh-YAML';
    }

    getServerName() {
        return 'Bosh-YAML';
    }

    activate() {
        require('atom-package-deps')
            .install('bosh-yaml')
            .then(() => console.debug('All dependencies installed, good to go'));
        super.activate();
    }

    launchVmArgs(jvm: JVM): Promise<string[]> {
        return Promise.resolve([
            '-Dorg.slf4j.simpleLogger.logFile=bosh-yaml.log',
            '-Dorg.slf4j.simpleLogger.defaultLogLevel=debug',
        ]);

    }

    sendConfig(server: ActiveServer) {
        server.connection.didChangeConfiguration({ settings: atom.config.get('bosh-yaml') });
    }

}