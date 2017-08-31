/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class RequestMappingSymbolProvider implements SymbolProvider {

	@Override
	public SymbolInformation getSymbol(Annotation node, TextDocument doc) {
		try {
			String path = getPath(node);
			String method = getMethod(node);

			if (path != null && !path.startsWith("/")) {
				path = "/" + path;
			}

			String symbolLabel = "@" + path + " -- " + method;

			SymbolInformation symbol = new SymbolInformation(symbolLabel, SymbolKind.Interface,
					new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength())));
			return symbol;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private String getMethod(Annotation node) {
		if (node.isNormalAnnotation()) {
			NormalAnnotation normNode = (NormalAnnotation) node;
			List<?> values = normNode.values();
			for (Iterator<?> iterator = values.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				if (object instanceof MemberValuePair) {
					MemberValuePair pair = (MemberValuePair) object;
					String valueName = pair.getName().getIdentifier();
					if (valueName != null && valueName.equals("method")) {
						Expression expression = pair.getValue();
						if (expression instanceof QualifiedName) {
							QualifiedName qualifiedName = (QualifiedName) expression;
							return qualifiedName.getName().toString();
						}
					}
				}
			}
		}

		String type = node.getTypeName().toString();
		if (type != null && type.endsWith("Mapping") && !type.startsWith("Request")) {
			String method = type.substring(0, type.length() - 7).toUpperCase();
			return method;
		}

		return "(no method defined)";
	}

	private String getPath(Annotation node) {
		if (node.isNormalAnnotation()) {
			NormalAnnotation normNode = (NormalAnnotation) node;
			List<?> values = normNode.values();
			for (Iterator<?> iterator = values.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				if (object instanceof MemberValuePair) {
					MemberValuePair pair = (MemberValuePair) object;
					String valueName = pair.getName().getIdentifier();
					if (valueName != null && (valueName.equals("value") || valueName.equals("path"))) {
						Expression expression = pair.getValue();
						if (expression instanceof StringLiteral) {
							StringLiteral literal = (StringLiteral) expression;
							return literal.getLiteralValue();
						}
					}
				}
			}
		} else if (node.isSingleMemberAnnotation()) {
			SingleMemberAnnotation singleNode = (SingleMemberAnnotation) node;
			Expression expression = singleNode.getValue();
			if (expression instanceof StringLiteral) {
				StringLiteral literal = (StringLiteral) expression;
				return literal.getLiteralValue();
			}
		}

		return null;
	}

}
