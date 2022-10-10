import { commands, Uri } from "vscode";
import { Emitter, LanguageClient } from "vscode-languageclient/node";
import { ExtensionAPI } from "./api";
import { LiveProcess, LiveProcessConnectedNotification, LiveProcessDisconnectedNotification, LiveProcessUpdatedNotification, LiveProcessGcPausesMetricsUpdatedNotification, LiveProcessMemoryMetricsUpdatedNotification } from "./notification";

export class ApiManager {
    public api: ExtensionAPI;
    private onDidLiveProcessConnectEmitter: Emitter<LiveProcess> = new Emitter<LiveProcess>();
    private onDidLiveProcessDisconnectEmitter: Emitter<LiveProcess> = new Emitter<LiveProcess>();
    private onDidLiveProcessUpdateEmitter: Emitter<LiveProcess> = new Emitter<LiveProcess>();
    private onDidLiveProcessGcPausesMetricsUpdateEmitter: Emitter<LiveProcess> = new Emitter<LiveProcess>();
    private onDidLiveProcessMemoryMetricsUpdateEmitter: Emitter<LiveProcess> = new Emitter<LiveProcess>();

    public constructor(client: LanguageClient) {
        const onDidLiveProcessConnect = this.onDidLiveProcessConnectEmitter.event;
        const onDidLiveProcessDisconnect = this.onDidLiveProcessDisconnectEmitter.event;
        const onDidLiveProcessUpdate = this.onDidLiveProcessUpdateEmitter.event;
        const onDidLiveProcessGcPausesMetricsUpdate = this.onDidLiveProcessGcPausesMetricsUpdateEmitter.event;
        const onDidLiveProcessMemoryMetricsUpdate = this.onDidLiveProcessMemoryMetricsUpdateEmitter.event;

        const COMMAND_LIVEDATA_GET = "sts/livedata/get";
        const getLiveProcessData = async (query) => {
            return await commands.executeCommand(COMMAND_LIVEDATA_GET, query);
        }

        const COMMAND_LIVEDATA_REFRESH = "sts/livedata/refresh";
        const refreshLiveProcessData = async (query) => {
            return await commands.executeCommand(COMMAND_LIVEDATA_REFRESH, query);
        }

        const COMMAND_LIVEDATA_LIST_CONNECTED = "sts/livedata/listConnected"
        const listConnectedProcesses = async () : Promise<LiveProcess[]> => {
            return await commands.executeCommand(COMMAND_LIVEDATA_LIST_CONNECTED);
        }

        const COMMAND_LIVEDATA_GET_METRICS = "sts/livedata/get/metrics"
        const getLiveProcessMetricsData = async (query) : Promise<LiveProcess[]> => {
            console.log(query);
            console.log("in get live metrics function");
            return await commands.executeCommand(COMMAND_LIVEDATA_GET_METRICS, query);
        }

        const COMMAND_LIVEDATA_REFRESH_METRICS = "sts/livedata/refresh/metrics";
        const refreshLiveProcessMetricsData = async (query) => {
            console.log("in get live metrics refresh function");
            return await commands.executeCommand(COMMAND_LIVEDATA_REFRESH_METRICS, query);
        }

        client.onNotification(LiveProcessConnectedNotification.type, (process: LiveProcess) => this.onDidLiveProcessConnectEmitter.fire(process));
        client.onNotification(LiveProcessDisconnectedNotification.type, (process: LiveProcess) => this.onDidLiveProcessDisconnectEmitter.fire(process));
        client.onNotification(LiveProcessUpdatedNotification.type, (process: LiveProcess) => this.onDidLiveProcessUpdateEmitter.fire(process));
        client.onNotification(LiveProcessGcPausesMetricsUpdatedNotification.type, (process: LiveProcess) => this.onDidLiveProcessGcPausesMetricsUpdateEmitter.fire(process));
        client.onNotification(LiveProcessMemoryMetricsUpdatedNotification.type, (process: LiveProcess) => this.onDidLiveProcessMemoryMetricsUpdateEmitter.fire(process));

        this.api = {
            client,
            onDidLiveProcessConnect,
            onDidLiveProcessDisconnect,
            onDidLiveProcessUpdate,
            onDidLiveProcessMemoryMetricsUpdate,
            onDidLiveProcessGcPausesMetricsUpdate,
            getLiveProcessData,
            refreshLiveProcessData,
            getLiveProcessMetricsData,
            refreshLiveProcessMetricsData,
            listConnectedProcesses,
        };
    }
}
