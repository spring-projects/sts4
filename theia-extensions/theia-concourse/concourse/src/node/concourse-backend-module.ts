import { ContainerModule } from "inversify";
import { LanguageServerContribution } from "@theia/languages/lib/node";
import { ConcourseLanguageContribution } from './concourse-language-contribution';

export default new ContainerModule(bind => {
    bind(LanguageServerContribution).to(ConcourseLanguageContribution).inSingletonScope();
});