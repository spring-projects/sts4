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
import { injectable } from 'inversify';
import { StsLanguageServerContribution } from '@pivotal-tools/theia-languageclient/lib/node/language-server-contribution';
import { BOSH_SERVER_ID, BOSH_SERVER_NAME } from '../common';

@injectable()
export class BoshLanguageContribution extends StsLanguageServerContribution {

    readonly id = BOSH_SERVER_ID;
    readonly name = BOSH_SERVER_NAME;
    protected readonly lsJarContainerFolder = path.resolve(__dirname, '../../jars');
    protected readonly lsJarGlob = 'bosh-language-server*.jar';
    protected readonly jvmArguments = [
        // '-Xdebug',
        // '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=7999',
        // '-Dlog.level=ALL',
        '-Dorg.slf4j.simpleLogger.logFile=bosh-yaml.log'
    ];

}