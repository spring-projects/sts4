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