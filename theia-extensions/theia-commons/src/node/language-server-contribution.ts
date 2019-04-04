/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
import * as path from 'path';
import * as glob from 'glob';
import { injectable } from 'inversify';
import { DEBUG_MODE } from '@theia/core/lib/node';
import { parseArgs } from '@theia/process/lib/node/utils';
import {IConnection, BaseLanguageServerContribution, LanguageServerStartOptions} from '@theia/languages/lib/node';
import {findJdk, findJvm, JVM} from '@pivotal-tools/jvm-launch-utils';
import {JavaLsProcessParameters} from '../common';

@injectable()
export abstract class StsLanguageServerContribution extends BaseLanguageServerContribution {

    protected readonly preferJdk: boolean = false;

    protected readonly lsJarContainerFolder: string = path.resolve(__dirname, '../../jars');

    protected readonly lsJarGlob: string = 'language-server*.jar';

    protected readonly jvmArguments: string[] = [];

    protected getJarPath() {
        const jarPaths = glob.sync(this.lsJarGlob, { cwd: this.lsJarContainerFolder });
        if (jarPaths.length === 0) {
            throw new Error(`The ${this.name} server launcher is not found`);
        }
        return path.resolve(this.lsJarContainerFolder, jarPaths[0]);
    }

    protected findJvm(javaHome?: string) {
        return this.preferJdk ? findJdk(javaHome) : findJvm(javaHome);
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

    async start(clientConnection: IConnection, { parameters }: JavaLsStartOptions) {
        const jarPath = this.getJarPath();

        const userJavaHome =
            (parameters && parameters.javahome)
            || process.env.STS_LSP_JAVA_HOME;

        const args = parseArgs(
            (parameters && parameters.vmargs)
            || process.env.STS_LSP_JAVA_VMARGS
            || undefined
        );

        const jvm = await this.findJvm(userJavaHome);

        this.validate(jvm);

        const server = await this.startSocketServer();
        const socket = this.accept(server);
        const env = Object.create(process.env);
        const addressInfo = server.address();
        if (typeof addressInfo === 'string') {
            throw new Error(`Address info was string ${addressInfo}`);
        }

        env.CLIENT_HOST = addressInfo.address;
        env.CLIENT_PORT = addressInfo.port;

        const command = jvm.getJavaExecutable();
        args.push(
            '-Dsts.lsp.client=theia',
            '-Dlsp.completions.indentation.enable=true',
            '-Dlsp.yaml.completions.errors.disable=true',
            `-Dserver.port=${env.CLIENT_PORT}`
        );

        let toolsJar = jvm.getToolsJar();
        if (toolsJar) {
            args.push("-Dloader.path="+toolsJar);
        }

        args.push(...this.jvmArguments);

        if (DEBUG_MODE) {
            args.push(
                '-Xdebug',
                '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=7999',
                '-Dlog.level=ALL'
            );
        }

        args.push(
            '-jar',
            jarPath
        );

        const serverConnection = await this.createProcessSocketConnection(socket, socket, command, args, { env });
        this.forward(clientConnection, serverConnection);
    }

}

export interface JavaLsStartOptions extends LanguageServerStartOptions {
    parameters?: JavaLsProcessParameters;
}