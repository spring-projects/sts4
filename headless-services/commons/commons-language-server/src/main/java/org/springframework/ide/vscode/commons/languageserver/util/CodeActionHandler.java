package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public interface CodeActionHandler {
	
	List<Either<Command, CodeAction>> handle(CancelChecker cancelToken, CodeActionCapabilities capabilities, TextDocument doc, IRegion region);

}
