/**
 * Generated using theia-extension-generator
 */

import { ContainerModule } from "inversify";
import { ConcourseClientContribution } from './language-client-contribution';
import { LanguageClientContribution } from '@theia/languages/lib/browser';
import {PipelineYamlGrammarContribution} from './pipeline-yaml-grammar-contribution';
import {TaskYamlGrammarContribution} from './task-yaml-grammar-contribution';
import {LanguageGrammarDefinitionContribution} from '@theia/monaco/lib/browser/textmate';

export default new ContainerModule(bind => {
    // add your contribution bindings here
    bind(LanguageClientContribution).to(ConcourseClientContribution).inSingletonScope();
    bind(LanguageGrammarDefinitionContribution).to(PipelineYamlGrammarContribution).inSingletonScope();
    bind(LanguageGrammarDefinitionContribution).to(TaskYamlGrammarContribution).inSingletonScope();
});