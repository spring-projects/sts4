/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.data.jpa.queries;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.springframework.ide.vscode.boot.java.handlers.Reconciler;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.java.properties.antlr.parser.AntlrParser;
import org.springframework.ide.vscode.java.properties.parser.ParseResults;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.KeyValuePair;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Value;

public class NamedQueryPropertiesReconcileEngine implements IReconcileEngine {
	
	private final JavaProjectFinder projectFinder;

	public NamedQueryPropertiesReconcileEngine(JavaProjectFinder projectFinder) {
		this.projectFinder = projectFinder;
	}

	@Override
	public void reconcile(IDocument doc, IProblemCollector problemCollector) {
		try {
			Reconciler reconciler = projectFinder.find(new TextDocumentIdentifier(doc.getUri()))
					.map(p -> SpringProjectUtil.hasDependencyStartingWith(p, "spring-data-jpa", null) ? new HqlReconciler() : new JpqlReconciler())
					.orElse(new JpqlReconciler());
			
			AntlrParser parser = new AntlrParser();
			ParseResults parseResults = parser.parse(doc.get());
			for (KeyValuePair pair : parseResults.ast.getNodes(KeyValuePair.class)) {
				Value value = pair.getValue();
				reconciler.reconcile(value.decode(), value.getOffset(), problemCollector);
			}
		} finally {
			problemCollector.endCollecting();
		}
	}

}
