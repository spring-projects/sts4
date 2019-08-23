package org.springframework.ide.vscode.commons.sprotty.scan;

import java.util.function.Consumer;

import org.eclipse.sprotty.ActionMessage;
import org.eclipse.sprotty.IDiagramServer;

public interface DiagramServerManager /*extends IDiagramServer.Provider*/ {
	
	void setRemoteEndpoint(Consumer<ActionMessage> remoteEndpoint);
	
	void sendMessageToServer(ActionMessage message);

}
