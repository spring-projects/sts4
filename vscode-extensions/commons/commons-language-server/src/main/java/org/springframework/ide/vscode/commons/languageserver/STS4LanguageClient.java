package org.springframework.ide.vscode.commons.languageserver;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Some 'custom' extensions to standard LSP {@link LanguageClient}.
 *
 * @author
 */
public interface STS4LanguageClient extends LanguageClient {

	@JsonNotification("sts/progress")
	void progress(ProgressParams progressEvent);

}
