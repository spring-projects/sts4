import { SpringBootClientContribution} from './language-client-contribution';
import { LanguageClientContribution } from '@theia/languages/lib/browser';
import { ContainerModule } from 'inversify';

import './monaco-yaml-contribution';
import './monaco-properties-contribution';
import {HighlightService} from "./highlight-service";
import {ClasspathService} from "./classpath-service";

export default new ContainerModule(bind => {
    // add your contribution bindings here
    bind(LanguageClientContribution).to(SpringBootClientContribution).inSingletonScope();
    bind(HighlightService).toSelf().inSingletonScope();
    bind(ClasspathService).toSelf().inSingletonScope();
});