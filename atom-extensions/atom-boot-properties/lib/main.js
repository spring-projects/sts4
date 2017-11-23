const path = require('path');
const { JavaProcessLanguageClient } = require('pivotal-atom-languageclient-commons');
const PROPERTIES = require('../properties.json');

class BootPropertiesClient extends JavaProcessLanguageClient {

    constructor() {
        //noinspection JSAnnotator
        super(
            PROPERTIES.jarUrl,
            path.join(__dirname, '..', 'server'),
            'boot-properties-language-server.jar'
        );
    }

    getGrammarScopes() {
        return ['source.boot-properties','source.boot-properties-yaml'];
    }

    getLanguageName() {
        return 'Boot-Properties';
    }

    getServerName() {
        return 'Boot-Properties';
    }

    activate() {
        // replace the example argument 'linter-ruby' with the name of this Atom package
        require('atom-package-deps')
            .install('boot-properties')
            .then(() => console.debug('All dependencies installed, good to go'));
        super.activate();
    }

    launchVmArgs(version) {
        return Promise.resolve([
            '-Dorg.slf4j.simpleLogger.logFile=boot-properties.log',
            '-Dorg.slf4j.simpleLogger.defaultLogLevel=debug',
        ]);
    }

}

module.exports = new BootPropertiesClient();
