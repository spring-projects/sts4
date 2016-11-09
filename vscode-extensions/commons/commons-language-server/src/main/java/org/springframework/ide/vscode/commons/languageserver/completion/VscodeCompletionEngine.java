package org.springframework.ide.vscode.commons.languageserver.completion;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.TextDocumentPositionParams;

/**
 * Interface that needs to be implemented by a 'completion engine' which can be easily
 * wired-up to provide completions for a Vscode language server.
 */
public interface VscodeCompletionEngine {
	CompletableFuture<CompletionList> getCompletions(TextDocumentPositionParams params);
	CompletableFuture<CompletionItem> resolveCompletion(CompletionItem unresolved);
}
