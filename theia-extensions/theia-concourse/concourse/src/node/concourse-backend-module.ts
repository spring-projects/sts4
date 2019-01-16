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
import { ContainerModule } from "inversify";
import { LanguageServerContribution } from "@theia/languages/lib/node";
import { ConcourseLanguageContribution } from './concourse-language-contribution';

export default new ContainerModule(bind => {
    bind(LanguageServerContribution).to(ConcourseLanguageContribution).inSingletonScope();
});