package org.springframework.ide.vscode.util;

import java.util.concurrent.CompletableFuture;

import io.typefox.lsapi.CompletionList;
import io.typefox.lsapi.TextDocumentPositionParams;

@FunctionalInterface
public interface CompletionHandler {
	CompletableFuture<CompletionList> handle(TextDocumentPositionParams params);
}
