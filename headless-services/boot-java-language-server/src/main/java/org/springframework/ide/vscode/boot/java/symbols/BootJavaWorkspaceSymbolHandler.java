package org.springframework.ide.vscode.boot.java.symbols;

import java.util.List;

import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.WorkspaceSymbolHandler;

public class BootJavaWorkspaceSymbolHandler implements WorkspaceSymbolHandler {

	private SimpleLanguageServer server;
	private JavaProjectFinder projectFinder;

	public BootJavaWorkspaceSymbolHandler(BootJavaLanguageServer bootJavaLanguageServer, JavaProjectFinder javaProjectFinder) {
		this.server = server;
		this.projectFinder = projectFinder;
	}

	@Override
	public List<? extends SymbolInformation> handle(WorkspaceSymbolParams params) {
		return SimpleTextDocumentService.NO_SYMBOLS;
	}

}
