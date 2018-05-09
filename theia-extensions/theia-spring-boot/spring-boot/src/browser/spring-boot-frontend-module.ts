import { SpringBootClientContribution} from './language-client-contribution';
import { LanguageClientContribution } from '@theia/languages/lib/browser';
import { ContainerModule } from 'inversify';

import './monaco-yaml-contribution';
import './monaco-properties-contribution';
import {HighlightService} from "./highlight-service";

export default new ContainerModule(bind => {
    // add your contribution bindings here
    bind(HighlightService).toSelf().inSingletonScope();
    bind(LanguageClientContribution).to(SpringBootClientContribution).inSingletonScope();
});