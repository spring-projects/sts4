/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.factories;

import java.util.Map;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.java.Boot3JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemCategory.Toggle.Option;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.java.properties.antlr.parser.AntlrParser;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.KeyValuePair;

public class SpringFactoriesReconcileEngine implements IReconcileEngine {
	
	@FunctionalInterface
	interface KeyValuePairReconciler {
		void reconcile(IJavaProject project, KeyValuePair pair, IProblemCollector problemCollector);
	}
	
	private final JavaProjectFinder projectFinder;
	private final AntlrParser parser = new AntlrParser();	
	private final Map<String, KeyValuePairReconciler> keyValuePairReconcilers;

	public SpringFactoriesReconcileEngine(JavaProjectFinder projectFinder, BootJavaConfig config) {
		this.projectFinder = projectFinder;
		
		keyValuePairReconcilers = Map.of(
				"org.springframework.boot.autoconfigure.EnableAutoConfiguration", (project, pair, problemCollector) -> {
					Option applicability = config.getProblemApplicability(Boot3JavaProblemType.FACTORIES_KEY_NOT_SUPPORTED);
					if (applicability == Option.ON || (applicability == Option.AUTO && SpringProjectUtil.springBootVersionGreaterOrEqual(3, 0, 0).test(project))) {
						ReconcileProblemImpl problem = new ReconcileProblemImpl(
								Boot3JavaProblemType.FACTORIES_KEY_NOT_SUPPORTED,
								"Key is not supported as of Spring Boot 3. See Boot 3 documentation topic \"Locating Auto-configuration Candidates\"",
								pair.getOffset(),
								pair.getLength());
						problemCollector.accept(problem);
					}
				}
		);
	}

	@Override
	public void reconcile(IDocument doc, IProblemCollector problemCollector) {
		projectFinder.find(new TextDocumentIdentifier(doc.getUri())).ifPresent(project -> {
			problemCollector.beginCollecting();
			try {
				PropertiesAst ast = parser.parse(doc.get()).ast;
				if (ast != null) {
					for (KeyValuePair pair : ast.getNodes(KeyValuePair.class)) {
						String key = pair.getKey().decode().trim();
						KeyValuePairReconciler r = keyValuePairReconcilers.get(key);
						if (r != null) {
							r.reconcile(project, pair, problemCollector);
						}
					}
				}
			} finally {
				problemCollector.endCollecting();
			}
		});
	}

}
