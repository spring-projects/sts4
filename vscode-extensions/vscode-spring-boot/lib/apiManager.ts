import { commands, Uri } from "vscode";
import { Emitter, LanguageClient } from "vscode-languageclient/node";
import { ExtensionAPI } from "./api";
import { LiveProcess, LiveProcessConnectedNotification, LiveProcessDisconnectedNotification, LiveProcessUpdatedNotification } from "./notification";

export class ApiManager {
    public api: ExtensionAPI;
    private onDidLiveProcessConnectEmitter: Emitter<LiveProcess> = new Emitter<LiveProcess>();
    private onDidLiveProcessDisconnectEmitter: Emitter<LiveProcess> = new Emitter<LiveProcess>();
    private onDidLiveProcessUpdateEmitter: Emitter<LiveProcess> = new Emitter<LiveProcess>();

    public constructor(client: LanguageClient) {
        const onDidLiveProcessConnect = this.onDidLiveProcessConnectEmitter.event;
        const onDidLiveProcessDisconnect = this.onDidLiveProcessDisconnectEmitter.event;
        const onDidLiveProcessUpdate = this.onDidLiveProcessUpdateEmitter.event;

        const COMMAND_LIVEDATA_GET = "sts/livedata/get";
        const getLiveProcessData = async (query) => {
            return await commands.executeCommand(COMMAND_LIVEDATA_GET, query);
        }

        const COMMAND_LIVEDATA_LIST_CONNECTED = "sts/livedata/listConnected"
        const listConnectedProcesses = async () : Promise<LiveProcess[]> => {
            return await commands.executeCommand(COMMAND_LIVEDATA_LIST_CONNECTED);
        }

        client.onNotification(LiveProcessConnectedNotification.type, (process: LiveProcess) => this.onDidLiveProcessConnectEmitter.fire(process));
        client.onNotification(LiveProcessDisconnectedNotification.type, (process: LiveProcess) => this.onDidLiveProcessDisconnectEmitter.fire(process));
        client.onNotification(LiveProcessUpdatedNotification.type, (process: LiveProcess) => this.onDidLiveProcessUpdateEmitter.fire(process));

        this.api = {
            client,
            onDidLiveProcessConnect,
            onDidLiveProcessDisconnect,
            onDidLiveProcessUpdate,
            getLiveProcessData,
            listConnectedProcesses,
        };
    }
}
