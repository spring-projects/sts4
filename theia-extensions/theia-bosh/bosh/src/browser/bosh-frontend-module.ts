import { ContainerModule } from 'inversify';
import { LanguageClientContribution } from '@theia/languages/lib/browser';
import { BoshClientContribution } from './language-client-contribution';
import { bindBoshPreferences } from './bosh-preferences';

// Contribute monaco-editor languages for deployment and cloud-config yaml
import './deployment-yaml-monaco-contribution';
import './cloudconfig-yaml-monaco-contribution';

export default new ContainerModule(bind => {
    // add your contribution bindings here
    bindBoshPreferences(bind);
    bind(LanguageClientContribution).to(BoshClientContribution).inSingletonScope();
});