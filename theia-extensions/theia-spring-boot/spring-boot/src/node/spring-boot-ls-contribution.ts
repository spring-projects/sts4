import * as path from 'path';
import * as glob from 'glob';
import { injectable } from 'inversify';
// import { DEBUG_MODE } from '@theia/core/lib/node';
import { IConnection, BaseLanguageServerContribution } from '@theia/languages/lib/node';
import { SPRING_BOOT_SERVER_ID, SPRING_BOOT_SERVER_NAME } from '../common';
import { findJdk } from '@pivotal-tools/jvm-launch-utils';


@injectable()
export class SpringBootLsContribution extends BaseLanguageServerContribution {

    readonly id = SPRING_BOOT_SERVER_ID;
    readonly name = SPRING_BOOT_SERVER_NAME;

    start(clientConnection: IConnection): void {
        const serverPath = path.resolve(__dirname, '../../jars');
        const jarPaths = glob.sync('spring-boot-language-server*.jar', { cwd: serverPath });
        if (jarPaths.length === 0) {
            throw new Error('The Spring Boot server launcher is not found.');
        }

        const jarPath = path.resolve(serverPath, jarPaths[0]);
        findJdk()
            .catch(error => {
                throw new Error('Error trying to find JVM');
            })
            .then(jvm => {
                if (!jvm) {
                    throw new Error("Couldn't locate java in $JAVA_HOME or $PATH");
                }
                let version = jvm.getMajorVersion();
                if (version<1) {
                    throw new Error(
                        'No compatible Java Runtime Environment found. The Java Runtime Environment is either below version "1.8" or is missing from the system'
                    );
                }

                if (!jvm.isJdk()) {
                    // TODO: show message that functionality is limited for non-JDK
                    // this.showErrorMessage(
                    //     '"Boot-Java" Package Functionality Limited',
                    //     'JAVA_HOME or PATH environment variable seems to point to a JRE. A JDK is required, hence Boot Hints are unavailable.'
                    // );
                }

                this.startSocketServer().then(server => {
                    const socket = this.accept(server);

                    // this.logInfo('logs at ' + path.resolve(workspacePath, '.metadata', '.log'));
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
                        '-Dorg.slf4j.simpleLogger.logFile=boot-java.log'
                    ];

                    args.push(`-Dserver.port=${env.CLIENT_PORT}`);

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