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
package org.springframework.ide.vscode.boot.properties;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.boot.BootPropertiesLanguageServer;
import org.springframework.ide.vscode.boot.properties.completions.SpringPropertiesCompletionEngine;
import org.springframework.ide.vscode.boot.properties.hover.PropertiesHoverInfoProvider;
import org.springframework.ide.vscode.boot.properties.reconcile.SpringPropertiesReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter.HoverType;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.TextDocument;

/**
 * Deprecated: This should not be used. In fact it isn't used anymore, except for in the tests. 
 * But these tests should be changed to test the 'merged' {@link BootPropertiesLanguageServer}
 * instead.
 */
@Deprecated
public class ApplicationPropertiesLanguageServer extends SimpleLanguageServer {
	
	private SpringPropertyIndexProvider indexProvider;
	private TypeUtilProvider typeUtilProvider;
	private VscodeCompletionEngineAdapter completionEngine;
	private SpringPropertiesReconcileEngine reconcileEngine;
	private VscodeHoverEngineAdapter hoverEngine;
	
	public ApplicationPropertiesLanguageServer(SpringPropertyIndexProvider indexProvider, TypeUtilProvider typeUtilProvider, JavaProjectFinder javaProjectFinder) {
		this.indexProvider = indexProvider;
		this.typeUtilProvider = typeUtilProvider;
		SimpleTextDocumentService documents = getTextDocumentService();

		reconcileEngine = getReconcileEngine();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			validateWith(doc, reconcileEngine);
		});
		
		SpringPropertiesCompletionEngine propertiesCompletionEngine = new SpringPropertiesCompletionEngine(
				indexProvider, 
				typeUtilProvider, 
				javaProjectFinder
		);
		completionEngine = new VscodeCompletionEngineAdapter(this, propertiesCompletionEngine);
		completionEngine.setMaxCompletionsNumber(100);
		documents.onCompletion(completionEngine::getCompletions);
		documents.onCompletionResolve(completionEngine::resolveCompletion);
		
		PropertiesHoverInfoProvider hoverInfoProvider = new PropertiesHoverInfoProvider(indexProvider, typeUtilProvider, javaProjectFinder);
		hoverEngine = new VscodeHoverEngineAdapter(this, hoverInfoProvider);
		documents.onHover(hoverEngine::getHover);
	}
	
	public void setMaxCompletionsNumber(int number) {
		completionEngine.setMaxCompletionsNumber(number);
	}
	
	public void setHoverType(HoverType type) {
		hoverEngine.setHoverType(type);
	}
	
	@Override
	protected ServerCapabilities getServerCapabilities() {
		ServerCapabilities c = new ServerCapabilities();
		
		c.setTextDocumentSync(TextDocumentSyncKind.Full);
		
		CompletionOptions completionProvider = new CompletionOptions();
		completionProvider.setResolveProvider(false);
		c.setCompletionProvider(completionProvider);
		
		c.setHoverProvider(true);
		
		return c;
	}
	
	protected SpringPropertiesReconcileEngine getReconcileEngine() {
		return new SpringPropertiesReconcileEngine(indexProvider, typeUtilProvider);
	}


}
