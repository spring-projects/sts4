import { commands, Uri } from "vscode";
import { Emitter, LanguageClient } from "vscode-languageclient/node";
import { ExtensionAPI } from "./api";
import { LiveProcessConnectedNotification, LiveProcessDisconnectedNotification } from "./notification";

export class ApiManager {
    public api: ExtensionAPI;
    private onDidLiveProcessConnectEmitter: Emitter<string> = new Emitter<string>();
    private onDidLiveProcessDisconnectEmitter: Emitter<string> = new Emitter<string>();

    public constructor(private client: LanguageClient) {
        const onDidLiveProcessConnect = this.onDidLiveProcessConnectEmitter.event;
        const onDidLiveProcessDisconnect = this.onDidLiveProcessDisconnectEmitter.event;

        const COMMAND_LIVEDATA_GET = "sts/livedata/get";
        const getLiveProcessData = async (query) => {
            await commands.executeCommand(COMMAND_LIVEDATA_GET, query);
        }

        // TODO: STS server should send corresponding notification back.
        client.onNotification(LiveProcessConnectedNotification.type, (processKey: string) => this.onDidLiveProcessConnectEmitter.fire(processKey));
        client.onNotification(LiveProcessDisconnectedNotification.type, (processKey: string) => this.onDidLiveProcessDisconnectEmitter.fire(processKey));

        this.api = {
            client,
            onDidLiveProcessConnect,
            onDidLiveProcessDisconnect,
            getLiveProcessData
        };
    }
}
