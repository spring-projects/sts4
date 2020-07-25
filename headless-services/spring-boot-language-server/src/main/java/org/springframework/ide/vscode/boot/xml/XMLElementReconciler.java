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

import java.util.List;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMNode;
import org.springframework.ide.vscode.boot.java.handlers.Reconciler;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;

/**
 * @author mlippert
 */
public class XMLElementReconciler {

	private final XMLElementKey xmlElementKey;
	private final String prefix;
	private final String postfix;
	private final Reconciler reconciler;

	public XMLElementReconciler(XMLElementKey xmlElementKey, String prefix, String postfix, Reconciler reconciler) {
		this.xmlElementKey = xmlElementKey;
		this.prefix = prefix;
		this.postfix = postfix;
		this.reconciler = reconciler;
	}

	public void visit(DOMNode node, IProblemCollector problemCollector) {
		if (!this.xmlElementKey.getNamespaceURI().equals(node.getNamespaceURI())) {
			return;
		}
		
		if (!this.xmlElementKey.getElementName().equals(node.getLocalName())) {
			return;
		}
		
		if (this.xmlElementKey.getParentNodeName() != null && node.getParentNode() != null && !this.xmlElementKey.getParentNodeName().equals(node.getParentNode().getLocalName())) {
			return;
		}
		
		if (this.xmlElementKey.getAttributeName() == null) {
			return;
		}
		
		List<DOMAttr> attributeNodes = node.getAttributeNodes();
		for (DOMAttr attributeNode : attributeNodes) {
			if (this.xmlElementKey.getAttributeName().equals(attributeNode.getLocalName())) {
				visitAttributeNode(attributeNode, problemCollector);
			}
		}
	}

	private void visitAttributeNode(DOMAttr attributeNode, IProblemCollector problemCollector) {
		DOMNode valueNode = attributeNode.getNodeAttrValue();
		int start = valueNode.getStart();
		
		String value = attributeNode.getNodeValue();
		
		if (value != null && value.startsWith(prefix) && value.endsWith(postfix)) {
			String valueToReconcile = value.substring(prefix.length(), value.length() - postfix.length());
			reconciler.reconcile(valueToReconcile, start + prefix.length() + 1, problemCollector);
		}
	}

}
