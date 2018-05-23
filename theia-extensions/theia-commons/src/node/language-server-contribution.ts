import * as path from 'path';
import * as glob from 'glob';
import { injectable } from 'inversify';
import { DEBUG_MODE } from '@theia/core/lib/node';
import { IConnection, BaseLanguageServerContribution } from '@theia/languages/lib/node';
import {findJdk, findJvm, JVM} from '@pivotal-tools/jvm-launch-utils';


@injectable()
export abstract class StsLanguageServerContribution extends BaseLanguageServerContribution {

    protected readonly preferJdk = false;

    protected readonly lsJarContainerFolder = path.resolve(__dirname, '../../jars');

    protected readonly lsJarGlob = 'language-server*.jar';

    protected readonly jvmArguments = [];

    protected getJarPath() {
        const jarPaths = glob.sync(this.lsJarGlob, { cwd: this.lsJarContainerFolder });
        if (jarPaths.length === 0) {
            throw new Error(`The ${this.name} server launcher is not found`);
        }
        return path.resolve(this.lsJarContainerFolder, jarPaths[0]);
    }

    protected findJvm() {
        return this.preferJdk ? findJdk() : findJvm();
    }

    protected validate(jvm: JVM) {
        if (!jvm) {
            throw new Error("Couldn't locate java in $JAVA_HOME or $PATH");
        }
        let version = jvm.getMajorVersion();
        if (version<8) {
            throw new Error(
                'No compatible Java Runtime Environment found. The Java Runtime Environment is either below version "1.8" or is missing from the system'
            );
        }
    }

    start(clientConnection: IConnection): void {
        const jarPath = this.getJarPath();
        this.findJvm()
            .catch(error => {
                throw new Error('Error trying to find JVM');
            })
            .then(jvm => {
                this.validate(jvm);
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
                        `-Dserver.port=${env.CLIENT_PORT}`
                    ];

                    args.push(...this.jvmArguments);

                    if (DEBUG_MODE) {
                        args.push(
                            '-Xdebug',
                            '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=7999',
                            '-Dlog.level=ALL'
                        );
                    }

                    args.push(
                        '-jar', jarPath,
                    );

                    this.createProcessSocketConnection(socket, socket, command, args, { env })
                        .then(serverConnection => this.forward(clientConnection, serverConnection));
                });

            });

    }
}