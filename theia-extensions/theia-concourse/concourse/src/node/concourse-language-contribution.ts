/*
 * Copyright (C) 2017 TypeFox and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 */

import * as path from 'path';
import * as glob from 'glob';
import { injectable } from 'inversify';
// import { DEBUG_MODE } from '@theia/core/lib/node';
import { IConnection, BaseLanguageServerContribution } from '@theia/languages/lib/node';
import { CONCOURSE_SERVER_ID, CONCOURSE_SERVER_NAME } from '../common';
import { findJvm } from '@pivotal-tools/jvm-launch-utils';


@injectable()
export class ConcourseLanguageContribution extends BaseLanguageServerContribution {

    readonly id = CONCOURSE_SERVER_ID;
    readonly name = CONCOURSE_SERVER_NAME;

    start(clientConnection: IConnection): void {
        const serverPath = path.resolve(__dirname, '../../server');
        const jarPaths = glob.sync('concourse-language-server*.jar', { cwd: serverPath });
        if (jarPaths.length === 0) {
            throw new Error('The Concourse YAML server launcher is not found.');
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
                        '-Dorg.slf4j.simpleLogger.logFile=concourse-yaml.log',
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