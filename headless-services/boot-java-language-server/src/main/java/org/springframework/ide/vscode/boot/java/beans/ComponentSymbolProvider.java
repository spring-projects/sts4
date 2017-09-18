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
package org.springframework.ide.vscode.boot.java.beans;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class ComponentSymbolProvider implements SymbolProvider {

	@Override
	public SymbolInformation getSymbol(Annotation node, TextDocument doc) {
		try {
			StringBuilder symbolLabel = new StringBuilder();
			symbolLabel.append("@Bean");

			String beanName = getBeanName(node);
			String beanType = getBeanType(node);

			symbolLabel.append(' ');
			symbolLabel.append(beanName);
			symbolLabel.append(' ');
			symbolLabel.append(beanType);

			SymbolInformation symbol = new SymbolInformation(symbolLabel.toString(), SymbolKind.Interface,
					new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength())));
			return symbol;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getBeanName(Annotation node) {
		ASTNode parent = node.getParent();
		if (parent instanceof TypeDeclaration) {
			TypeDeclaration type = (TypeDeclaration) parent;

			String beanName = type.getName().toString();
			if (beanName.length() > 0 && Character.isUpperCase(beanName.charAt(0))) {
				beanName = Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
			}
			return beanName;
		}
		return null;
	}

	private String getBeanType(Annotation node) {
		ASTNode parent = node.getParent();
		if (parent instanceof TypeDeclaration) {
			TypeDeclaration type = (TypeDeclaration) parent;
			String returnType = type.resolveBinding().getQualifiedName();
			return returnType;
		}
		return null;
	}

}
