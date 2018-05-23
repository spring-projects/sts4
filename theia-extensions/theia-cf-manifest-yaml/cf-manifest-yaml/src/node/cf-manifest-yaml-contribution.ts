import * as path from 'path';
import { injectable } from 'inversify';
import { CF_MANIFEST_YAML_LANGUAGE_ID, CF_MANIFEST_YAML_LANGUAGE_NAME } from '../common';
import { StsLanguageServerContribution } from "@pivotal-tools/theia-languageclient/lib/node/language-server-contribution";

@injectable()
export class CfManifestYamlContribution extends StsLanguageServerContribution {

    readonly id = CF_MANIFEST_YAML_LANGUAGE_ID;
    readonly name = CF_MANIFEST_YAML_LANGUAGE_NAME;
    protected readonly lsJarContainerFolder = path.resolve(__dirname, '../../jars');
    protected readonly lsJarGlob = 'manifest-yaml-language-server*.jar';
    protected readonly jvmArguments = [
        // '-Xdebug',
        // '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=7999',
        // '-Dlog.level=ALL',
        '-Dorg.slf4j.simpleLogger.logFile=cf-manifest-yaml.log'
    ];

}