package org.springframework.ide.vscode.commons.languageserver.sprotty;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;

import com.google.gson.JsonObject;

public interface SprottyService {
	
	@JsonNotification("sts/sprotty")
	void sprottyMessage(JsonObject message);

}
