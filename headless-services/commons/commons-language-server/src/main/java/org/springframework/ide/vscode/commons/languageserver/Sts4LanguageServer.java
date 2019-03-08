/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver;

import org.eclipse.lsp4j.services.LanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleWorkspaceService;

/**
 * STS4 language server
 *
 * @author Alex Boyko
 *
 */
public interface Sts4LanguageServer extends LanguageServer {

	@Override
	SimpleTextDocumentService getTextDocumentService();

	@Override
	SimpleWorkspaceService getWorkspaceService();

	/**
	 * Progress Service to report progress info from LS to the client
	 * @return progress service instance
	 */
	ProgressService getProgressService();

	/**
	 * Diagnostic service to report errors/warnings from LS to the client
	 * @return
	 */
	DiagnosticService getDiagnosticService();

}
