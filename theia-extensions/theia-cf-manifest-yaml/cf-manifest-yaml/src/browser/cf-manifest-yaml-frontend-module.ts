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
import { CfManifestYamlClientContribution } from './language-client-contribution';
import { LanguageClientContribution } from '@theia/languages/lib/browser';
import { ContainerModule } from 'inversify';
import {ManifestYamlGrammarContribution} from './manifest-yaml-grammar-contribution';
import {LanguageGrammarDefinitionContribution} from '@theia/monaco/lib/browser/textmate';
import {bindCfManifestYamlPreferences} from './cf-manifest-preferences';

export default new ContainerModule(bind => {
    // add your contribution bindings here
    bindCfManifestYamlPreferences(bind);
    bind(LanguageClientContribution).to(CfManifestYamlClientContribution).inSingletonScope();
    bind(LanguageGrammarDefinitionContribution).to(ManifestYamlGrammarContribution).inSingletonScope();
});