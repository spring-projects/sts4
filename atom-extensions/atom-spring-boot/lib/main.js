const path = require('path');
const { JavaProcessLanguageClient, StsAdapter } = require('pivotal-atom-languageclient-commons');
const { Convert } = require('atom-languageclient');
const PROPERTIES = require('../properties.json');

const BOOT_DATA_MARKER_TYPE = 'BootApp-Hint';
const BOOT_HINT_GUTTER_NAME = 'boot-hint-gutter';

class SpringBootLanguageClient extends JavaProcessLanguageClient {

    constructor() {
        //noinspection JSAnnotator
        super(
            PROPERTIES.jarUrl,
            path.join(__dirname, '..', 'server'),
            'spring-boot-language-server.jar'
        );
        // this.DEBUG = true;
    }

    postInitialization(server) {
        this.sendConfig(server);
        this._disposable.add(atom.config.observe('boot-java', () => this.sendConfig(server)));
    }

    sendConfig(server) {
        server.connection.didChangeConfiguration({ settings: {'boot-java': atom.config.get('boot-java') }});
    }

    getGrammarScopes() {
        return ['source.java', 'source.boot-properties', 'source.boot-properties-yaml'];
    }

    getLanguageName() {
        return 'spring-boot';
    }

    getServerName() {
        return 'Spring Boot';
    }

    activate() {
        require('atom-package-deps')
            .install('spring-boot')
            .then(() => console.debug('All dependencies installed, good to go'));
        super.activate();
    }

    getOrInstallLauncher() {
        return 'org.springframework.boot.loader.JarLauncher';
    }

    preferJdk() {
        return true;
    }

    launchVmArgs(jvm) {
        let vmargs = [
            // '-Xdebug',
            // '-agentlib:jdwp=transport=dt_socket,server=y,address=7999,suspend=n',
            '-Dorg.slf4j.simpleLogger.logFile=boot-java.log',
            '-Dorg.slf4j.simpleLogger.defaultLogLevel=debug',
        ];
        if (!jvm.isJdk()) {
            this.showErrorMessage(
                '"Boot-Java" Package Functionality Limited', 
                'JAVA_HOME or PATH environment variable seems to point to a JRE. A JDK is required, hence Boot Hints are unavailable.'
            );
        }
        let toolsJar = jvm.getToolsJar();
        vmargs.push(
            "-cp",
            `${toolsJar ? `${toolsJar}${path.delimiter}` : ''}${this.getServerJar()}`
        );
        return Promise.resolve(vmargs);
    }

    createStsAdapter() {
        return new BootStsAdapter();
    }

    filterChangeWatchedFiles(filePath) {
        return filePath.endsWith('.gradle') || filePath.endsWith(path.join('', 'pom.xml'));
    }

}

class BootStsAdapter extends StsAdapter {

    constructor() {
        super();
    }

    onHighlight(params) {
        this.findEditors(params.doc.uri).forEach(editor => this.markHintsForEditor(editor, params.ranges));
    }

    markHintsForEditor(editor, ranges) {
        editor.findMarkers(BOOT_DATA_MARKER_TYPE).forEach(m => m.destroy());
        if (Array.isArray(ranges)) {
            ranges.forEach(range => this.createHintMarker(editor, range));
        }
        const gutter = editor.gutterWithName(BOOT_HINT_GUTTER_NAME);
        if (gutter) {
            if (!ranges || !ranges.length) {
                gutter.hide();
            } else if (!gutter.isVisible()) {
                gutter.show();
            }
        }
    }

    createHintMarker(editor, range) {
        // Create marker model
        const marker = editor.markBufferRange(Convert.lsRangeToAtomRange(range), BOOT_DATA_MARKER_TYPE);

        // Marker around the text in the editor
        editor.decorateMarker(marker, {
            type: 'highlight',
            class: 'boot-hint'
        });

        // Marker in the diagnostic gutter
        let gutter = editor.gutterWithName(BOOT_HINT_GUTTER_NAME);
        if (!gutter) {
            gutter = editor.addGutter({
                name: BOOT_HINT_GUTTER_NAME,
                visible: false,
            });
        }
        const iconElement = document.createElement('span');
        iconElement.setAttribute('class', 'gutter-boot-hint');
        gutter.decorateMarker(marker, {item: iconElement});
    }
}

module.exports = new SpringBootLanguageClient();
