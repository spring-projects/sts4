const path = require('path');
const { JarLanguageClient } = require('pivotal-atom-languageclient-commons');

class ManifestYamlLanguageClient extends JarLanguageClient {

    constructor() {
        super(
            'https://s3-us-west-1.amazonaws.com/s3-test.spring.io/sts4/fatjars/snapshots/manifest-yaml-language-server-0.0.9-201707270057.jar',
            path.join(__dirname, '..', 'server')
        );

        this.statusElement = document.createElement('span');
        this.statusElement.className = 'inline-block';
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

    handleDownlaodPercentChange(bytesDone, size, percent) {
        this.updateStatusBar(`downloading ${percent}%`);
    }

    handleServerInstalled() {
        this.updateStatusBar('installed');
    }

    consumeStatusBar (statusBar) {
        this.statusTile = statusBar.addRightTile({ item: this.statusElement, priority: 1000 });
    }

    updateStatusBar (text) {
        this.statusElement.textContent = `${this.name} ${text}`;
    }

    launchVmArgs(version) {
        return [
            '-Dlsp.yaml.completions.errors.disable=true',
            '-Xdebug',
            '-agentlib:jdwp=transport=dt_socket,address=9000,server=y,suspend=n',
            '-Dorg.slf4j.simpleLogger.logFile=manifest-yaml.log',
            '-Dorg.slf4j.simpleLogger.defaultLogLevel=debug',
        ];

    }

}

module.exports = new ManifestYamlLanguageClient();
