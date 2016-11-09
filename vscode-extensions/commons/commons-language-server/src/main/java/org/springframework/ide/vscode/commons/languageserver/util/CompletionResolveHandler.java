package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;

@FunctionalInterface
public interface CompletionResolveHandler {
	CompletableFuture<CompletionItem> handle(CompletionItem unresolved);
}
