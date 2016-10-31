package org.springframework.ide.vscode.manifest.yaml;

import java.util.concurrent.CompletableFuture;

import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.CompletionList;
import io.typefox.lsapi.TextDocumentPositionParams;

/**
 * Interface that needs to be implemented by a 'completion engine' which can be easily
 * wired-up to provide completions for a Vscode language server.
 */
public interface VscodeCompletionEngine {
	CompletableFuture<CompletionList> getCompletions(TextDocumentPositionParams params);
	CompletableFuture<CompletionItem> resolveCompletion(CompletionItem unresolved);
}
