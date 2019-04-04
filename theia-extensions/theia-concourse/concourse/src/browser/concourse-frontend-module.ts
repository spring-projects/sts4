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
import { ContainerModule } from "inversify";
import { ConcourseClientContribution } from './language-client-contribution';
import { LanguageClientContribution } from '@theia/languages/lib/browser';
import {PipelineYamlGrammarContribution} from './pipeline-yaml-grammar-contribution';
import {TaskYamlGrammarContribution} from './task-yaml-grammar-contribution';
import {LanguageGrammarDefinitionContribution} from '@theia/monaco/lib/browser/textmate';
import {bindConcourseYamlPreferences} from './concourse-preferences';

export default new ContainerModule(bind => {
    // add your contribution bindings here
    bindConcourseYamlPreferences(bind);
    bind(LanguageClientContribution).to(ConcourseClientContribution).inSingletonScope();
    bind(LanguageGrammarDefinitionContribution).to(PipelineYamlGrammarContribution).inSingletonScope();
    bind(LanguageGrammarDefinitionContribution).to(TaskYamlGrammarContribution).inSingletonScope();
});