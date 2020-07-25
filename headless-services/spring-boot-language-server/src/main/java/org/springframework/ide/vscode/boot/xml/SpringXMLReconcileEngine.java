/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml;

import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.BEANS_NAMESPACE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.BEAN_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.VALUE_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.PROPERTY_ELEMENT;

import java.net.URI;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.handlers.SpelExpressionReconciler;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * @author Martin Lippert
 */
public class SpringXMLReconcileEngine implements IReconcileEngine {

	private static final Logger log = LoggerFactory.getLogger(SpringXMLReconcileEngine.class);

	private final JavaProjectFinder projectFinder;
	private final SpelExpressionReconciler spelExpressionReconciler;
	private final XMLElementReconciler[] reconcilers;

	public SpringXMLReconcileEngine(JavaProjectFinder projectFinder) {
		this.projectFinder = projectFinder;
		
		this.spelExpressionReconciler = new SpelExpressionReconciler();

		this.reconcilers = new XMLElementReconciler[] {

				new XMLElementReconciler(new XMLElementKey(BEANS_NAMESPACE, BEAN_ELEMENT, PROPERTY_ELEMENT, VALUE_ATTRIBUTE), "#{", "}", spelExpressionReconciler)

		};
	}

	public void setSpelExpressionSyntaxValidationEnabled(boolean spelExpressionValidationEnabled) {
		this.spelExpressionReconciler.setEnabled(spelExpressionValidationEnabled);
	}

	@Override
	public void reconcile(final IDocument doc, final IProblemCollector problemCollector) {
		IJavaProject project = projectFinder.find(new TextDocumentIdentifier(doc.getUri())).orElse(null);
		URI uri = URI.create(doc.getUri());
		
		if (project != null) {
			
			try {
				problemCollector.beginCollecting();
				reconcileXML(doc, problemCollector);
			}
			finally {
				problemCollector.endCollecting();
			}
		}
	}
	
	private void reconcileXML(final IDocument doc, final IProblemCollector problemCollector) {
		String content = doc.get();

		DOMParser parser = DOMParser.getInstance();
		DOMDocument dom = parser.parse(content, "", null);
		reconcileNode(dom, problemCollector);
	}

	private void reconcileNode(DOMNode node, IProblemCollector problemCollector) {
		reconcile(node, problemCollector);
		
		for (DOMNode domNode : node.getChildren()) {
			reconcileNode(domNode, problemCollector);
		}
	}

	private void reconcile(DOMNode node, IProblemCollector problemCollector) {
		for (int i = 0; i < reconcilers.length; i++) {
			reconcilers[i].visit(node, problemCollector);
		}
	}

}
