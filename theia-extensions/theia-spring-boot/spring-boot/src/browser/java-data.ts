/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
import {injectable, inject} from 'inversify';
import {CommandRegistry} from '@theia/core/lib/common';
import {ILanguageClient} from '@theia/languages/lib/browser';

export const JAVA_TYPE_REQUEST_TYPE = 'sts/javaType';
export const JAVADOC_HOVER_LINK_REQUEST_TYPE = 'sts/javadocHoverLink';
export const JAVA_LOCATION_REQUEST_TYPE = 'sts/javaLocation';
export const JAVADOC_REQUEST_TYPE = 'sts/javadoc';

@injectable()
export class JavaDataService {

    constructor(
        @inject(CommandRegistry) protected readonly commands: CommandRegistry
    ) {
    }

    attach(client: ILanguageClient) {
        client.onRequest(JAVA_TYPE_REQUEST_TYPE, (params: JavaDataParams) => this.commands.executeCommand('sts.java.type', params));
        client.onRequest(JAVADOC_HOVER_LINK_REQUEST_TYPE, (params: JavaDataParams) => this.commands.executeCommand('sts.java.javadocHoverLink', params));
        client.onRequest(JAVA_LOCATION_REQUEST_TYPE, (params: JavaDataParams) => this.commands.executeCommand('sts.java.location', params));
        client.onRequest(JAVADOC_REQUEST_TYPE, (params: JavaDataParams) => this.commands.executeCommand('sts.java.javadoc', params));
    }

}

interface JavaDataParams {
    projectUri?: string;
    bindingKey: string;
    lookInOtherProjects?: boolean
}