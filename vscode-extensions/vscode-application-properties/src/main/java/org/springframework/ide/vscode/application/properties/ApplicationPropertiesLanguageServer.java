/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.application.properties;

import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.application.properties.reconcile.SpringPropertiesReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.TextDocument;

/**
 * Language Server for Spring Boot Application Properties files
 * 
 * @author Alex Boyko
 *
 */
public class ApplicationPropertiesLanguageServer extends SimpleLanguageServer {
	
	private SpringPropertyIndexProvider indexProvider;
	private TypeUtilProvider typeUtilProvider;


	public ApplicationPropertiesLanguageServer(SpringPropertyIndexProvider indexProvider, TypeUtilProvider typeUtilProvider) {
		this.indexProvider = indexProvider;
		this.typeUtilProvider = typeUtilProvider;
		SimpleTextDocumentService documents = getTextDocumentService();

		IReconcileEngine reconcileEngine = getReconcileEngine();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			validateWith(doc, reconcileEngine);
		});
	}
	
	@Override
	protected ServerCapabilities getServerCapabilities() {
		ServerCapabilities c = new ServerCapabilities();
		
		c.setTextDocumentSync(TextDocumentSyncKind.Full);
		
		return c;
	}
	
	protected IReconcileEngine getReconcileEngine() {
		return new SpringPropertiesReconcileEngine(indexProvider, typeUtilProvider);
	}


}
