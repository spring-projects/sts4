import { ContainerModule } from 'inversify';
import { LanguageServerContribution } from '@theia/languages/lib/node';
import { SpringBootLsContribution } from './spring-boot-ls-contribution';
import { BootJavaExtension } from './java-extension';
import { JavaExtensionContribution } from '@theia/java/lib/node';

export default new ContainerModule(bind => {
    bind(LanguageServerContribution).to(SpringBootLsContribution).inSingletonScope();
    bind(JavaExtensionContribution).to(BootJavaExtension).inSingletonScope();
});