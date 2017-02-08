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
package org.springframework.ide.vscode.boot.java;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.springframework.ide.vscode.boot.java.completions.BootJavaCompletionEngine;
import org.springframework.ide.vscode.boot.java.completions.BootJavaReconcileEngine;
import org.springframework.ide.vscode.commons.gradle.GradleCore;
import org.springframework.ide.vscode.commons.gradle.GradleProjectFinderStrategy;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.java.DefaultJavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.IJavaProjectFinderStrategy;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.maven.JavaProjectWithClasspathFileFinderStrategy;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.MavenProjectFinderStrategy;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * Language Server for Spring Boot Application Properties files
 * 
 * @author Martin Lippert
 */
public class BootJavaLanguageServer extends SimpleLanguageServer {
	
	public static final JavaProjectFinder DEFAULT_PROJECT_FINDER = new DefaultJavaProjectFinder(new IJavaProjectFinderStrategy[] {
			new MavenProjectFinderStrategy(MavenCore.getDefault()),
			new GradleProjectFinderStrategy(GradleCore.getDefault()),
			new JavaProjectWithClasspathFileFinderStrategy()
	});

	private final JavaProjectFinder javaProjectFinder;
	private final VscodeCompletionEngineAdapter completionEngine;
	
	public BootJavaLanguageServer(JavaProjectFinder javaProjectFinder) {
		this.javaProjectFinder = javaProjectFinder;
		SimpleTextDocumentService documents = getTextDocumentService();

		IReconcileEngine reconcileEngine = new BootJavaReconcileEngine();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			validateWith(doc, reconcileEngine);
		});
		
		ICompletionEngine bootCompletionEngine = new BootJavaCompletionEngine(javaProjectFinder);
		completionEngine = new VscodeCompletionEngineAdapter(this, bootCompletionEngine);
		completionEngine.setMaxCompletionsNumber(100);
		documents.onCompletion(completionEngine::getCompletions);
		documents.onCompletionResolve(completionEngine::resolveCompletion);
	}

	public void setMaxCompletionsNumber(int number) {
		completionEngine.setMaxCompletionsNumber(number);
	}
	
	@Override
	protected ServerCapabilities getServerCapabilities() {
		ServerCapabilities c = new ServerCapabilities();
		
		c.setTextDocumentSync(TextDocumentSyncKind.Incremental);
		CompletionOptions completionProvider = new CompletionOptions();
		completionProvider.setResolveProvider(false);
		c.setCompletionProvider(completionProvider);
		
		return c;
	}
	
}
