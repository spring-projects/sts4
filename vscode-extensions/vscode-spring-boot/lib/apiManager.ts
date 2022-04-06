import { commands, Uri } from "vscode";
import { Emitter, LanguageClient } from "vscode-languageclient/node";
import { ExtensionAPI } from "./api";
import { LiveProcessConnectedNotification, LiveProcessDisconnectedNotification, LiveProcessUpdatedNotification } from "./notification";

export class ApiManager {
    public api: ExtensionAPI;
    private onDidLiveProcessConnectEmitter: Emitter<string> = new Emitter<string>();
    private onDidLiveProcessDisconnectEmitter: Emitter<string> = new Emitter<string>();
    private onDidLiveProcessUpdateEmitter: Emitter<string> = new Emitter<string>();

    public constructor(client: LanguageClient) {
        const onDidLiveProcessConnect = this.onDidLiveProcessConnectEmitter.event;
        const onDidLiveProcessDisconnect = this.onDidLiveProcessDisconnectEmitter.event;
        const onDidLiveProcessUpdate = this.onDidLiveProcessUpdateEmitter.event;

        const COMMAND_LIVEDATA_GET = "sts/livedata/get";
        const getLiveProcessData = async (query) => {
            return await commands.executeCommand(COMMAND_LIVEDATA_GET, query);
        }

        client.onNotification(LiveProcessConnectedNotification.type, (processKey: string) => this.onDidLiveProcessConnectEmitter.fire(processKey));
        client.onNotification(LiveProcessDisconnectedNotification.type, (processKey: string) => this.onDidLiveProcessDisconnectEmitter.fire(processKey));
        client.onNotification(LiveProcessUpdatedNotification.type, (processKey: string) => this.onDidLiveProcessUpdateEmitter.fire(processKey));

        this.api = {
            client,
            onDidLiveProcessConnect,
            onDidLiveProcessDisconnect,
            onDidLiveProcessUpdate,
            getLiveProcessData
        };
    }
}
