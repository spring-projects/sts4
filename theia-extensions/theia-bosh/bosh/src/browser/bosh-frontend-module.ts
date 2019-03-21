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
import { ContainerModule } from 'inversify';
import { LanguageClientContribution } from '@theia/languages/lib/browser';
import {LanguageGrammarDefinitionContribution} from '@theia/monaco/lib/browser/textmate';
import { BoshClientContribution } from './language-client-contribution';
import { bindBoshPreferences } from './bosh-preferences';
import {DeploymentYamlGrammarContribution} from './deployment-yaml-grammar-contribution';
import {CloudConfigYamlGrammarContribution} from './cloudconfig-yaml-grammar-contribution';

export default new ContainerModule(bind => {
    // add your contribution bindings here
    bindBoshPreferences(bind);
    bind(LanguageClientContribution).to(BoshClientContribution).inSingletonScope();
    bind(LanguageGrammarDefinitionContribution).to(DeploymentYamlGrammarContribution).inSingletonScope();
    bind(LanguageGrammarDefinitionContribution).to(CloudConfigYamlGrammarContribution).inSingletonScope();
});