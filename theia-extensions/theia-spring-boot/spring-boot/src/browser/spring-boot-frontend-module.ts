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
import '../../images/boot-icon.png';
import { SpringBootClientContribution} from './language-client-contribution';
import { LanguageClientContribution } from '@theia/languages/lib/browser';
import { ContainerModule } from 'inversify';
import { HighlightService } from './highlight-service';
import { bindBootPreferences } from './boot-preferences';
import { HighlightCodeLensService } from './codelens-service';
import { LanguageGrammarDefinitionContribution } from '@theia/monaco/lib/browser/textmate';
import { BootPropertiesGrammarContribution } from './boot-properties-grammar-contribution';
import { BootYamlGrammarContribution } from './boot-yaml-grammar-contribution';
import { ClasspathService } from './classpath-service';
import {JavaDataService} from './java-data';

export default new ContainerModule(bind => {
    // add your contribution bindings here
    bindBootPreferences(bind);
    bind(LanguageClientContribution).to(SpringBootClientContribution).inSingletonScope();
    bind(LanguageGrammarDefinitionContribution).to(BootPropertiesGrammarContribution).inSingletonScope();
    bind(LanguageGrammarDefinitionContribution).to(BootYamlGrammarContribution).inSingletonScope();
    bind(HighlightService).toSelf().inSingletonScope();
    bind(HighlightCodeLensService).toSelf().inSingletonScope();
    bind(ClasspathService).toSelf().inSingletonScope();
    bind(JavaDataService).toSelf().inRequestScope();
});