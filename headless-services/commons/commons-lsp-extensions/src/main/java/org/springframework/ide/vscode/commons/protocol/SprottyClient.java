package org.springframework.ide.vscode.commons.protocol;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;

import com.google.gson.JsonObject;

public interface SprottyClient {
	
	@JsonNotification("sts/progress")
	void sprottyMessage(JsonObject message);

}
