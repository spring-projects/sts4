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
import { CONCOURSE_SERVER_ID, CONCOURSE_SERVER_NAME } from '../common';

@injectable()
export class ConcourseLanguageContribution extends StsLanguageServerContribution {

    readonly id = CONCOURSE_SERVER_ID;
    readonly name = CONCOURSE_SERVER_NAME;
    protected readonly lsLocation = path.resolve(__dirname, '../../server/concourse-language-server');
    protected readonly configFileName = 'application.properties';
    protected readonly mainClass = 'org.springframework.ide.vscode.concourse.ConcourseLanguageServerBootApp';
    protected readonly jvmArguments = [
        // '-Xdebug',
        // '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=7999',
        // '-Dlog.level=ALL',
        '-Dorg.slf4j.simpleLogger.logFile=concourse-yaml.log'
    ];

}