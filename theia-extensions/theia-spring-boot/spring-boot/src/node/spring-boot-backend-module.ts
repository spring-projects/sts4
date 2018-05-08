import { ContainerModule } from 'inversify';
import { LanguageServerContribution } from '@theia/languages/lib/node';
import { SpringBootLsContribution } from './spring-boot-ls-contribution';

export default new ContainerModule(bind => {
    bind(LanguageServerContribution).to(SpringBootLsContribution).inSingletonScope();
});