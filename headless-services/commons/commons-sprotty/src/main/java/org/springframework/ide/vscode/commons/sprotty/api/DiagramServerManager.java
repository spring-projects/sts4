package org.springframework.ide.vscode.commons.sprotty.api;

import java.util.function.Consumer;

import org.eclipse.sprotty.ActionMessage;

public interface DiagramServerManager {
	
	void setRemoteEndpoint(Consumer<ActionMessage> remoteEndpoint);
	
	void sendMessageToServer(ActionMessage message);

}
