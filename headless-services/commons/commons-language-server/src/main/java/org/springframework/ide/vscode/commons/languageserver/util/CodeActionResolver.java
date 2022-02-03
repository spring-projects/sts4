package org.springframework.ide.vscode.commons.languageserver.util;

import org.eclipse.lsp4j.CodeAction;

public interface CodeActionResolver {
	
	void resolve(CodeAction codeAction);

}
