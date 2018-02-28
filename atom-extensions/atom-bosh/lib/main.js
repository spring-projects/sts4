const path = require('path');
const { JavaProcessLanguageClient } = require('@pivotal-tools/atom-languageclient-commons');
const PROPERTIES = require('../properties.json');

class BoshYamlClient extends JavaProcessLanguageClient {

    constructor() {
        //noinspection JSAnnotator
        super(
            PROPERTIES.jarUrl,
            path.join(__dirname, '..', 'server'),
            'bosh-language-server.jar'
        ); 
    }

    postInitialization(server) {
        this.sendConfig(server);
        this._disposable.add(atom.config.observe('bosh-yaml', () => this.sendConfig(server)));
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

    launchVmArgs(jvm) {
        return Promise.resolve([
            '-Dorg.slf4j.simpleLogger.logFile=bosh-yaml.log',
            '-Dorg.slf4j.simpleLogger.defaultLogLevel=debug',
        ]);

    }

    sendConfig(server) {
        server.connection.didChangeConfiguration({ settings: atom.config.get('bosh-yaml') });
    }

}

module.exports = new BoshYamlClient();
