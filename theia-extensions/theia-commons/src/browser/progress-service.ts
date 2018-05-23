import { injectable, inject } from 'inversify';
import { NotificationType } from 'vscode-jsonrpc';
import { CommandRegistry } from '@theia/core/lib/common';
import { ILanguageClient } from '@theia/languages/lib/common';

const PROGRESS_NOTIFICATION_TYPE = new NotificationType<ProgressParams,void>('sts/progress');

@injectable()
export class ProgressService {

    constructor(
        @inject(CommandRegistry) protected readonly commands: CommandRegistry
    ) {}

    attach(client: ILanguageClient) {
        client.onNotification(PROGRESS_NOTIFICATION_TYPE, params => this.progress(params));
    }

    private async progress(params: ProgressParams) {
    }

}

export interface ProgressParams {
    readonly id: string;
    readonly statusMsg: string;
}