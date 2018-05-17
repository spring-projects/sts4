/**
 * Generated using theia-extension-generator
 */

import { ContainerModule } from "inversify";
import { ConcourseClientContribution } from './language-client-contribution';
import { LanguageClientContribution } from '@theia/languages/lib/browser';

// Contribute monaco-editor languages for pipeline and task yaml
import './pipeline-yaml-monaco-contribution';
import './task-yaml-monaco-contribution';

export default new ContainerModule(bind => {
    // add your contribution bindings here
    bind(LanguageClientContribution).to(ConcourseClientContribution).inSingletonScope();
});