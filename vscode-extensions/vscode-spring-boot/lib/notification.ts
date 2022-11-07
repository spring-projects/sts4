import { NotificationType } from "vscode-languageclient";

/**
 * Common information provided by all live process notifications, for all types
 * of events and for all types of processes.
 */
export interface LiveProcess {
	type: string;
	processKey: string;
	processName: string;
}

/**
 * Specialized interface for type 'local' LiveProcess.
 */
export interface LocalLiveProcess extends LiveProcess {
	type: "local"
	pid: string
}

export namespace LiveProcessConnectedNotification {
	export const type = new NotificationType<LiveProcess>('sts/liveprocess/connected');
}

export namespace LiveProcessDisconnectedNotification {
	export const type = new NotificationType<LiveProcess>('sts/liveprocess/disconnected');
}

export namespace LiveProcessUpdatedNotification {
	export const type = new NotificationType<LiveProcess>('sts/liveprocess/updated');
}

export namespace LiveProcessGcPausesMetricsUpdatedNotification {
	export const type = new NotificationType<LiveProcess>('sts/liveprocess/gcpauses/metrics/updated');
}

export namespace LiveProcessMemoryMetricsUpdatedNotification {
	export const type = new NotificationType<LiveProcess>('sts/liveprocess/memory/metrics/updated');
}