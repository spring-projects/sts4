import { injectable, inject } from 'inversify';
import { Workspace, Languages, LanguageClientFactory } from '@theia/languages/lib/browser';
import { StsLanguageClientContribution } from '@pivotal-tools/theia-languageclient/lib/browser/language-client-contribution';
import {
    CONCOURSE_PIPELINE_YAML_LANGUAGE_ID, CONCOURSE_SERVER_ID, CONCOURSE_SERVER_NAME,
    CONCOURSE_TASK_YAML_LANGUAGE_ID
} from '../common';

@injectable()
export class ConcourseClientContribution extends StsLanguageClientContribution<null> {

    readonly id = CONCOURSE_SERVER_ID;
    readonly name = CONCOURSE_SERVER_NAME;

    constructor(
        @inject(Workspace) workspace: Workspace,
        @inject(Languages) languages: Languages,
        @inject(LanguageClientFactory) languageClientFactory: LanguageClientFactory,
    ) {
        super(workspace, languages, languageClientFactory);
    }

    protected get documentSelector() {
        return [CONCOURSE_PIPELINE_YAML_LANGUAGE_ID, CONCOURSE_TASK_YAML_LANGUAGE_ID];
    }

    protected get globPatterns() {
        return [
            '*pipeline*.yml',
            '*task.yml',
            '**/tasks/*.yml'
        ];
    }
}
