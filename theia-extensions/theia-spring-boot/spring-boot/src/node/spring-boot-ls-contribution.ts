import * as path from 'path';
import { injectable } from 'inversify';
import { SPRING_BOOT_SERVER_ID, SPRING_BOOT_SERVER_NAME } from '../common';
import { JVM } from '@pivotal-tools/jvm-launch-utils';
import { StsLanguageServerContribution } from '@pivotal-tools/theia-languageclient/lib/node/language-server-contribution';

@injectable()
export class SpringBootLsContribution extends StsLanguageServerContribution {

    readonly id = SPRING_BOOT_SERVER_ID;
    readonly name = SPRING_BOOT_SERVER_NAME;
    protected readonly lsJarContainerFolder = path.resolve(__dirname, '../../jars');
    protected readonly lsJarGlob = 'spring-boot-language-server*.jar';
    protected readonly jvmArguments = [
        // '-Xdebug',
        // '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=7999',
        // '-Dlog.level=ALL',
        '-Dorg.slf4j.simpleLogger.logFile=boot-java.log'
    ];

    validate(jvm: JVM) {
        if (!jvm.isJdk()) {
            // TODO: show message that functionality is limited for non-JDK
            // this.showErrorMessage(
            //     '"Boot-Java" Package Functionality Limited',
            //     'JAVA_HOME or PATH environment variable seems to point to a JRE. A JDK is required, hence Boot Hints are unavailable.'
            // );
        }
    }

}