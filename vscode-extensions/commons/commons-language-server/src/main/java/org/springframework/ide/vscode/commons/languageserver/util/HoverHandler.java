package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.TextDocumentPositionParams;

@FunctionalInterface
public interface HoverHandler {
	CompletableFuture<Hover> handle(TextDocumentPositionParams params);
}
