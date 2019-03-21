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
import {JavaExtensionContribution} from "@theia/java/lib/node";
import * as path from 'path';
import { injectable } from 'inversify';

@injectable()
export class BootJavaExtension implements JavaExtensionContribution {

    getExtensionBundles() {
        const jarFolderPath = path.resolve(__dirname, '../../jars');
        return [
            path.resolve(jarFolderPath, 'io.projectreactor.reactor-core.jar'),
            path.resolve(jarFolderPath, 'org.reactivestreams.reactive-streams.jar'),
            path.resolve(jarFolderPath, 'jdt-ls-commons.jar'),
            path.resolve(jarFolderPath, 'jdt-ls-extension.jar')
        ];
    }

}