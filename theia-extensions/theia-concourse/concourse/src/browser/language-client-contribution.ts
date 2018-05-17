import { injectable, inject } from 'inversify';
import { BaseLanguageClientContribution, Workspace, Languages, LanguageClientFactory } from '@theia/languages/lib/browser';
import {
    CONCOURSE_PIPELINE_YAML_LANGUAGE_ID, CONCOURSE_SERVER_ID, CONCOURSE_SERVER_NAME,
    CONCOURSE_TASK_YAML_LANGUAGE_ID
} from '../common';

@injectable()
export class ConcourseClientContribution extends BaseLanguageClientContribution {

    readonly id = CONCOURSE_SERVER_ID;
    readonly name = CONCOURSE_SERVER_NAME;

    constructor(
        @inject(Workspace) protected readonly workspace: Workspace,
        @inject(Languages) protected readonly languages: Languages,
        @inject(LanguageClientFactory) protected readonly languageClientFactory: LanguageClientFactory,
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
