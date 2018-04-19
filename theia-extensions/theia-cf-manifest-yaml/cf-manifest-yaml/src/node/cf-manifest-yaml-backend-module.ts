import { ContainerModule } from "inversify";
import { LanguageServerContribution } from "@theia/languages/lib/node";
import { CfManifestYamlContribution } from './cf-manifest-yaml-contribution';

export default new ContainerModule(bind => {
    bind(LanguageServerContribution).to(CfManifestYamlContribution).inSingletonScope();
});