import { injectable, inject, postConstruct } from 'inversify';
import { BaseLanguageClientContribution, Workspace, Languages, LanguageClientFactory } from '@theia/languages/lib/browser';
import { PreferenceProxy } from '@theia/core/lib/browser';
import { ProgressService } from './progress-service';
import { MoveCursorService } from './move-cursor-service';
import { Utils } from '../common/utils';

const CONFIG_CHANGED_NOTIFICATION_TYPE = 'workspace/didChangeConfiguration';

@injectable()
export abstract class StsLanguageClientContribution<P> extends BaseLanguageClientContribution {

    protected readonly preferences: PreferenceProxy<P> | undefined;

    @inject(ProgressService) protected readonly progressService: ProgressService;
    @inject(MoveCursorService) protected readonly moveCursorService: MoveCursorService;

    constructor(workspace: Workspace, languages: Languages, languageClientFactory: LanguageClientFactory) {
        super(workspace, languages, languageClientFactory);
    }

    @postConstruct()
    protected async init() {
        if (this.preferences) {
            await this.preferences.ready;

            // Send settings to LS
            this.sendConfig();
            this.preferences.onPreferenceChanged(() => this.sendConfig());
        }
        this.attachMessageHandlers();
    }

    protected attachMessageHandlers() {
        this.languageClient.then(client => {
            this.progressService.attach(client);
            this.moveCursorService.attach(client);
        });
    }

    private sendConfig() {
        return this.languageClient.then(client => {
            const params = Utils.convertDotToNested(Object.assign({}, this.preferences));
            return client.sendNotification(CONFIG_CHANGED_NOTIFICATION_TYPE, {
                settings: params
            });
        })
    }

}
