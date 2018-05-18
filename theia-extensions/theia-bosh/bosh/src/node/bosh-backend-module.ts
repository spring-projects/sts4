import { ContainerModule } from "inversify";
import { LanguageServerContribution } from "@theia/languages/lib/node";
import { BoshLanguageContribution } from './bosh-language-contribution';

export default new ContainerModule(bind => {
    bind(LanguageServerContribution).to(BoshLanguageContribution).inSingletonScope();
});