import * as path from 'path';
import { injectable } from 'inversify';
import { StsLanguageServerContribution } from '@pivotal-tools/theia-languageclient/lib/node/language-server-contribution';
import { CONCOURSE_SERVER_ID, CONCOURSE_SERVER_NAME } from '../common';

@injectable()
export class ConcourseLanguageContribution extends StsLanguageServerContribution {

    readonly id = CONCOURSE_SERVER_ID;
    readonly name = CONCOURSE_SERVER_NAME;
    protected readonly lsJarContainerFolder = path.resolve(__dirname, '../../jars');
    protected readonly lsJarGlob = 'concourse-language-server*.jar';
    protected readonly jvmArguments = [
        // '-Xdebug',
        // '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=7999',
        // '-Dlog.level=ALL',
        '-Dorg.slf4j.simpleLogger.logFile=concourse-yaml.log'
    ];

}