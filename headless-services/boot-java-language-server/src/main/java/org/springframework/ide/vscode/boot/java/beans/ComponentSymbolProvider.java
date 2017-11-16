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

import java.util.Collection;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 * @author Kris De Volder
 */
public class ComponentSymbolProvider implements SymbolProvider {

	private String annotationName;
	private String simpleAnnotationName;

	public ComponentSymbolProvider(String annotationName) {
		this.annotationName = annotationName;
		this.simpleAnnotationName = StringUtil.simpleName(annotationName);
	}

	@Override
	public Collection<SymbolInformation> getSymbols(Annotation node, ITypeBinding annotationType, TextDocument doc) {
		try {
			ImmutableList.Builder<SymbolInformation> symbols = ImmutableList.builder();
			symbols.add(createSymbol(node, doc, false));
			if (isExactAnnotationType(annotationType)) {
				symbols.add(createSymbol(node, doc, true));
			}
			return symbols.build();
		}
		catch (Exception e) {
			Log.log(e);
		}
		return ImmutableList.of();
	}

	protected SymbolInformation createSymbol(Annotation node, TextDocument doc, boolean isExactAnnotationType) throws BadLocationException {
		String annotationTypeName = isExactAnnotationType
				? simpleAnnotationName
				: '+' + simpleAnnotationName;
		String beanName = getBeanName(node);
		String beanType = getBeanType(node);

		SymbolInformation symbol = new SymbolInformation(
				beanLabel("+", annotationTypeName, beanName, beanType), SymbolKind.Interface,
				new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength())));
		return symbol;
	}

	protected boolean isExactAnnotationType(ITypeBinding actualAnnotationType) {
		return actualAnnotationType.getQualifiedName().equals(annotationName);
	}

	protected String beanLabel(String searchPrefix, String annotationTypeName, String beanName, String beanType) {
		StringBuilder symbolLabel = new StringBuilder();
		symbolLabel.append("@");
		symbolLabel.append(searchPrefix);
		symbolLabel.append(' ');
		symbolLabel.append('\'');
		symbolLabel.append(beanName);
		symbolLabel.append('\'');
		symbolLabel.append(" (@");
		symbolLabel.append(annotationTypeName);
		symbolLabel.append(") ");
		symbolLabel.append(beanType);
		return symbolLabel.toString();
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
			String returnType = type.resolveBinding().getName();
			return returnType;
		}
		return null;
	}

	@Override
	public String toString() {
		return "ComponentSymbolProvider("+simpleAnnotationName+")";
	}

}
