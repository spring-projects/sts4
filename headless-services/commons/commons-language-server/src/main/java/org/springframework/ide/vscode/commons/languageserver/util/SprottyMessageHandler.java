package org.springframework.ide.vscode.commons.languageserver.util;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface SprottyMessageHandler {
	void handleMessage(JsonObject message);
}
