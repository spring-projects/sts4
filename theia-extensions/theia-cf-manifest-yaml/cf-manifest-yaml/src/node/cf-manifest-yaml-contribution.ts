import * as path from 'path';
import * as glob from 'glob';
import { injectable } from 'inversify';
// import { DEBUG_MODE } from '@theia/core/lib/node';
import { IConnection, BaseLanguageServerContribution } from '@theia/languages/lib/node';
import { CF_MANIFEST_YAML_LANGUAGE_ID, CF_MANIFEST_YAML_LANGUAGE_NAME } from '../common';
import { findJvm } from '@pivotal-tools/jvm-launch-utils';


@injectable()
export class CfManifestYamlContribution extends BaseLanguageServerContribution {

    readonly id = CF_MANIFEST_YAML_LANGUAGE_ID;
    readonly name = CF_MANIFEST_YAML_LANGUAGE_NAME;

    start(clientConnection: IConnection): void {
        const serverPath = path.resolve(__dirname, '../../jars');
        const jarPaths = glob.sync('manifest-yaml-language-server*.jar', { cwd: serverPath });
        if (jarPaths.length === 0) {
            throw new Error('The CF Manifest YAML server launcher is not found.');
        }

        const jarPath = path.resolve(serverPath, jarPaths[0]);
        findJvm()
            .catch(error => {
                throw new Error('Error trying to find JVM');
            })
            .then(jvm => {
                if (!jvm) {
                    throw new Error("Couldn't locate java in $JAVA_HOME or $PATH");
                }

                this.startSocketServer().then(server => {
                    const socket = this.accept(server);

                    const env = Object.create(process.env);
                    const addressInfo = server.address();
                    if (typeof addressInfo === 'string') {
                        throw new Error(`Address info was string ${addressInfo}`);
                    }
                    env.CLIENT_HOST = addressInfo.address;
                    env.CLIENT_PORT = addressInfo.port;
                    const command = jvm.getJavaExecutable();
                    const args = [
                        '-Dsts.lsp.client=theia',
                        '-Dlsp.completions.indentation.enable=true',
                        '-Dlsp.yaml.completions.errors.disable=true',
                        '-Dorg.slf4j.simpleLogger.logFile=cf-manifest-yaml.log',
                        `-Dserver.port=${env.CLIENT_PORT}`
                    ];

                    // if (DEBUG_MODE) {
                        args.push(
                            '-Xdebug',
                            '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=7999',
                            '-Dlog.level=ALL'
                        );
                    // }

                    args.push(
                        '-jar', jarPath,
                    );

                    this.createProcessSocketConnection(socket, socket, command, args, { env })
                        .then(serverConnection => this.forward(clientConnection, serverConnection));
                });

            });

    }
}