const path = require('path');
const { JarLanguageClient } = require('pivotal-atom-languageclient-commons');
const PROPERTIES = require('../properties.json');

class BootJavaLanguageClient extends JarLanguageClient {

    constructor() {
        //noinspection JSAnnotator
        super(
            PROPERTIES.jarUrl,
            path.join(__dirname, '..', 'server'),
            'boot-java-language-server.jar'
        );
    }

    getGrammarScopes() {
        return ['source.java']
    }

    getLanguageName() {
        return 'boot-java'
    }

    getServerName() {
        return 'Spring Boot'
    }

    activate() {
        // replace the example argument 'linter-ruby' with the name of this Atom package
        require('atom-package-deps').install('boot-java').then(function() {
            console.log('All dependencies installed, good to go')
        });
        super.activate();
    }

    launchVmArgs(version) {
        return [
            '-Dorg.slf4j.simpleLogger.logFile=boot-java.log',
            '-Dorg.slf4j.simpleLogger.defaultLogLevel=debug',
            // '-Xdebug',
            // '-agentlib:jdwp=transport=dt_socket,server=y,address=7999,suspend=n'
        ];

    }

}

module.exports = new BootJavaLanguageClient();
