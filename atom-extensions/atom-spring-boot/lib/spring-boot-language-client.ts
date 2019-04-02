import * as path from 'path';
import {JavaProcessLanguageClient, JavaOptions} from '@pivotal-tools/atom-languageclient-commons';
import {BootStsAdapter} from './boot-sts-adapter';
import {ActiveServer} from 'atom-languageclient';
import {JVM} from '@pivotal-tools/jvm-launch-utils';

export class SpringBootLanguageClient extends JavaProcessLanguageClient {

    constructor() {
        //noinspection JSAnnotator
        super(
            path.join(__dirname, '..', 'server'),
            'spring-boot-language-server.jar'
        );
        // this.DEBUG = true;
    }

    protected postInitialization(server: ActiveServer) {
        super.postInitialization(server);
        this.sendConfig(server);
        (<any>this)._disposable.add(atom.config.observe('spring-boot', () => this.sendConfig(server)));
    }

    private sendConfig(server: ActiveServer) {
        server.connection.didChangeConfiguration({ settings: {'boot-java': atom.config.get('spring-boot') }});
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

    preferJdk() {
        return true;
    }

    launchVmArgs(jvm: JVM) {
        let vmargs = [
            // '-Xdebug',
            // '-agentlib:jdwp=transport=dt_socket,server=y,address=7999,suspend=y',
            '-Dorg.slf4j.simpleLogger.logFile=boot-java.log'
        ];
        if (!jvm.isJdk()) {
            this.showErrorMessage(
                '"Boot-Java" Package Functionality Limited',
                'JAVA_HOME or PATH environment variable seems to point to a JRE. A JDK is required, hence Boot Hints are unavailable.'
            );
        }
        return Promise.resolve(vmargs);
    }

    createStsAdapter() {
        return new BootStsAdapter();
    }

    filterChangeWatchedFiles(filePath: string) {
        return filePath.endsWith('.gradle') || filePath.endsWith(path.join('', 'pom.xml'));
    }

    getJavaOptions(): JavaOptions {
        const home = atom.config.get('spring-boot.java.home');
        const vmargs = atom.config.get('spring-boot.java.vmargs');
        return {
            home: typeof home === 'string' ? home : undefined,
            vmargs: Array.isArray(vmargs) ? vmargs :  undefined
        };
    }

}
