/*
 * Copyright (C) 2017 TypeFox and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 */

import * as path from 'path';
import * as glob from 'glob';
import { injectable } from "inversify";
// import { DEBUG_MODE } from '@theia/core/lib/node';
import { IConnection, BaseLanguageServerContribution } from "@theia/languages/lib/node";
import { CF_MANIFEST_YAML_LANGUAGE_ID, CF_MANIFEST_YAML_LANGUAGE_NAME } from '../common';
import {findJdk, findJvm, JVM} from '@pivotal-tools/jvm-launch-utils';


@injectable()
export class CfManifestYamlContribution extends BaseLanguageServerContribution {

    readonly id = CF_MANIFEST_YAML_LANGUAGE_ID;
    readonly name = CF_MANIFEST_YAML_LANGUAGE_NAME;

    preferJdk(): boolean {
        return false;
    }

    findJvm(): Promise<JVM | null> {
        return this.preferJdk() ? findJdk() : findJvm();
    }

    launchVmArgs(jvm: JVM): string[] {
        return [
            '-Dsts.lsp.client=theia',
            '-Dlsp.completions.indentation.enable=true',
            '-Dlsp.yaml.completions.errors.disable=true',
        ];
    }

    start(clientConnection: IConnection): void {
        const serverPath = path.resolve(__dirname, '../../server');
        const jarPaths = glob.sync('manifest-yaml-language-server*.jar', { cwd: serverPath });
        if (jarPaths.length === 0) {
            throw new Error('The CF Manifest YAML server launcher is not found.');
        }

        const jarPath = path.resolve(serverPath, jarPaths[0]);
        this.findJvm()
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

                this.startSocketServer().then(server => {
                    const socket = this.accept(server);

                    // this.logInfo('logs at ' + path.resolve(workspacePath, '.metadata', '.log'));
                    const env = Object.create(process.env);
                    env.CLIENT_HOST = server.address().address;
                    env.CLIENT_PORT = server.address().port;
                    const command = jvm.getJavaExecutable();
                    const args = this.launchVmArgs(jvm);

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