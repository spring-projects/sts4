import { ContainerModule } from 'inversify';
import { ClasspathService } from './classpath-service';
import { ProgressService } from './progress-service';
import { MoveCursorService } from './move-cursor-service';
import {LanguageGrammarDefinitionContribution} from '@theia/monaco/lib/browser/textmate';
import {JavaPropertiesGrammarContribution} from './java-properties-grammar-contribution';
import {YamlGrammarContribution} from './yaml-grammar-contribution';

export default new ContainerModule(bind => {
    // add your contribution bindings here
    bind(ClasspathService).toSelf().inSingletonScope();
    bind(ProgressService).toSelf().inSingletonScope();
    bind(MoveCursorService).toSelf().inSingletonScope();
    bind(LanguageGrammarDefinitionContribution).to(JavaPropertiesGrammarContribution).inSingletonScope();
    bind(LanguageGrammarDefinitionContribution).to(YamlGrammarContribution).inSingletonScope();
});