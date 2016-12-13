package org.springframework.ide.vscode.commons.languageserver.util;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

/**
 * Handler for $ messages. They should/could be ignored since they depend on the
 * implementation capabilities on the server.
 *
 * @author Alex Boyko
 *
 */
@JsonSegment("$")
public interface ServiceNotificationsClient {

	@JsonNotification
	default void setTraceNotification(Object param) {
		// Ignore Message
	}

	@JsonNotification
	default void logTraceNotification(Object param) {
		// Ignore Message
	}

}
