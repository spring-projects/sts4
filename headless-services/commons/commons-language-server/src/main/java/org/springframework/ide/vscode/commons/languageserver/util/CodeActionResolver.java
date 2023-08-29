package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.WorkspaceEdit;

public interface CodeActionResolver {
	
	CompletableFuture<WorkspaceEdit> resolve(CodeAction codeAction);

}
