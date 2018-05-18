import { injectable, inject, postConstruct } from 'inversify';
import { BaseLanguageClientContribution, Workspace, Languages, LanguageClientFactory } from '@theia/languages/lib/browser';
import { NotificationType } from 'vscode-jsonrpc';
import { DidChangeConfigurationParams } from 'vscode-base-languageclient/lib/base';
import {
    BOSH_DEPLOYMENT_YAML_LANGUAGE_ID,
    BOSH_CLOUDCONFIG_YAML_LANGUAGE_ID,
    BOSH_SERVER_ID,
    BOSH_SERVER_NAME
} from '../common';
import { BoshPreferences } from './bosh-preferences';
import { Utils } from './utils';

const CONFIG_CHANGED_NOTIFICATION_TYPE = new NotificationType<DidChangeConfigurationParams,void>('workspace/didChangeConfiguration');

@injectable()
export class BoshClientContribution extends BaseLanguageClientContribution {

    readonly id = BOSH_SERVER_ID;
    readonly name = BOSH_SERVER_NAME;

    constructor(
        @inject(Workspace) protected readonly workspace: Workspace,
        @inject(Languages) protected readonly languages: Languages,
        @inject(LanguageClientFactory) protected readonly languageClientFactory: LanguageClientFactory,
        @inject(BoshPreferences) protected readonly preferences: BoshPreferences
    ) {
        super(workspace, languages, languageClientFactory);
    }

    @postConstruct()
    protected async init() {
        await this.preferences.ready;
        // Send settings to LS
        this.sendConfig();
        this.preferences.onPreferenceChanged(() => this.sendConfig());
    }

    private sendConfig() {
        return this.languageClient.then(client => {
            const params = Utils.convertDotToNested(Object.assign({}, this.preferences));
            return client.sendNotification(CONFIG_CHANGED_NOTIFICATION_TYPE, {
                settings: params
            });
        })
    }

    protected get documentSelector() {
        return [BOSH_DEPLOYMENT_YAML_LANGUAGE_ID, BOSH_CLOUDCONFIG_YAML_LANGUAGE_ID];
    }

    protected get globPatterns() {
        return [
            '*deployment*.yml',
            '*cloud-config*.yml'
        ];
    }
}
