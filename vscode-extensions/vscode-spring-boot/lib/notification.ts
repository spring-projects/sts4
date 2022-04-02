import { NotificationType } from "vscode-languageclient";

export namespace LiveProcessConnectedNotification {
	export const type = new NotificationType<string>('sts/liveprocess/connected');
}

export namespace LiveProcessDisconnectedNotification {
	export const type = new NotificationType<string>('sts/liveprocess/disconnected');
}
