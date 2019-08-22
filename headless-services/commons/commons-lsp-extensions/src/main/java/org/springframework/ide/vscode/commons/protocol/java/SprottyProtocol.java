package org.springframework.ide.vscode.commons.protocol.java;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;

import com.google.gson.JsonObject;

public interface SprottyProtocol {
	
	@JsonNotification("sts/sprotty")
	void sprottyMessage(JsonObject message);

}
