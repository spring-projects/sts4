const path = require('path');
const { JavaProcessLanguageClient } = require('pivotal-atom-languageclient-commons');
const PROPERTIES = require('../properties.json');

class ConcourseCiYamlClient extends JavaProcessLanguageClient {

    constructor() {
        //noinspection JSAnnotator
        super(
            PROPERTIES.jarUrl,
            path.join(__dirname, '..', 'server'),
            'concourse-language-server.jar'
        );
    }

    getGrammarScopes() {
        return ['source.concourse-pipeline-yaml','source.concourse-task-yaml'];
    }

    getLanguageName() {
        return 'Concourse-Pipeline-YAML';
    }

    getServerName() {
        return 'Concourse-Pipeline-YAML';
    }

    activate() {
        // replace the example argument 'linter-ruby' with the name of this Atom package
        require('atom-package-deps')
            .install('concourse-pipeline-yaml')
            .then(() => console.debug('All dependencies installed, good to go'));
        super.activate();
    }

    launchVmArgs(version) {
        return Promise.resolve([
            '-Dorg.slf4j.simpleLogger.logFile=concourse-ci-yaml.log',
            '-Dorg.slf4j.simpleLogger.defaultLogLevel=debug',
        ]);

    }

}

module.exports = new ConcourseCiYamlClient();
