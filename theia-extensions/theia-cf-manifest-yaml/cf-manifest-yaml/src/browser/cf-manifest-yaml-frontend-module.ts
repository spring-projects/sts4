/**
 * Generated using theia-extension-generator
 */

import { CfManifestYamlClientContribution } from './language-client-contribution';
import { LanguageClientContribution } from '@theia/languages/lib/browser';
import { ContainerModule } from 'inversify';
import {ManifestYamlGrammarContribution} from './manifest-yaml-grammar-contribution';
import {LanguageGrammarDefinitionContribution} from '@theia/monaco/lib/browser/textmate';

export default new ContainerModule(bind => {
    // add your contribution bindings here    
    bind(LanguageClientContribution).to(CfManifestYamlClientContribution).inSingletonScope();
    bind(LanguageGrammarDefinitionContribution).to(ManifestYamlGrammarContribution).inSingletonScope();
});