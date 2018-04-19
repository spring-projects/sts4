/**
 * Generated using theia-extension-generator
 */

import { CfManifestYamlClientContribution } from './language-client-contribution';
import { LanguageClientContribution } from "@theia/languages/lib/browser";

import { ContainerModule } from "inversify";

import "./monaco-contribution";

export default new ContainerModule(bind => {
    // add your contribution bindings here    
    bind(LanguageClientContribution).to(CfManifestYamlClientContribution).inSingletonScope();
});