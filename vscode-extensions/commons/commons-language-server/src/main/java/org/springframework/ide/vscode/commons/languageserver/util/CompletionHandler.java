package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.TextDocumentPositionParams;

@FunctionalInterface
public interface CompletionHandler {
	CompletableFuture<CompletionList> handle(TextDocumentPositionParams params);
}
