import '../../images/boot-icon.png';
import { SpringBootClientContribution} from './language-client-contribution';
import { LanguageClientContribution } from '@theia/languages/lib/browser';
import { ContainerModule } from 'inversify';
import { HighlightService } from './highlight-service';
import { bindBootPreferences } from './boot-preferences';
import { HighlightCodeLensService } from './codelens-service';
import { LanguageGrammarDefinitionContribution } from '@theia/monaco/lib/browser/textmate';
import { BootPropertiesGrammarContribution } from './boot-properties-grammar-contribution';
import { BootYamlGrammarContribution } from './boot-yaml-grammar-contribution';
import { ClasspathService } from './classpath-service';

export default new ContainerModule(bind => {
    // add your contribution bindings here
    bindBootPreferences(bind);
    bind(LanguageClientContribution).to(SpringBootClientContribution).inSingletonScope();
    bind(LanguageGrammarDefinitionContribution).to(BootPropertiesGrammarContribution).inSingletonScope();
    bind(LanguageGrammarDefinitionContribution).to(BootYamlGrammarContribution).inSingletonScope();
    bind(HighlightService).toSelf().inSingletonScope();
    bind(HighlightCodeLensService).toSelf().inSingletonScope();
    bind(ClasspathService).toSelf().inSingletonScope();
});