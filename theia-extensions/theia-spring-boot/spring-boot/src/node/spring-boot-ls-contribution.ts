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
import { SPRING_BOOT_SERVER_ID, SPRING_BOOT_SERVER_NAME } from '../common';
import { JVM } from '@pivotal-tools/jvm-launch-utils';
import { StsLanguageServerContribution } from '@pivotal-tools/theia-languageclient/lib/node/language-server-contribution';

@injectable()
export class SpringBootLsContribution extends StsLanguageServerContribution {

    readonly id = SPRING_BOOT_SERVER_ID;
    readonly name = SPRING_BOOT_SERVER_NAME;
    protected readonly preferJdk = true;
    protected readonly lsLocation = path.resolve(__dirname, '../../language-server');
    protected readonly configFileName = 'application.properties';
    protected readonly mainClass = 'org.springframework.ide.vscode.boot.app.BootLanguagServerBootApp';
    protected readonly jvmArguments = [
        // '-Xdebug',
        // '-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=7999',
        // '-Dlog.level=ALL',
        '-Dorg.slf4j.simpleLogger.logFile=boot-java.log'
    ];

    validate(jvm: JVM) {
        if (!jvm.isJdk()) {
            // TODO: show message that functionality is limited for non-JDK
            // this.showErrorMessage(
            //     '"Boot-Java" Package Functionality Limited',
            //     'JAVA_HOME or PATH environment variable seems to point to a JRE. A JDK is required, hence Boot Hints are unavailable.'
            // );
        }
    }

}