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
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Optionals;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class BeansSymbolProvider implements SymbolProvider {

	private static final String FUNCTION_FUNCTION_TYPE = Function.class.getName();
	private static final String FUNCTION_CONSUMER_TYPE = Consumer.class.getName();
	private static final String FUNCTION_SUPPLIER_TYPE = Supplier.class.getName();

	@Override
	public Collection<SymbolInformation> getSymbols(Annotation node, TextDocument doc) {
		try {
			if (isFunctionBean(node)) {
				return Collections.singleton(createFunctionSymbol(node, doc));
			}
			else {
				return Collections.singleton(createRegularBeanSymbol(node, doc));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean isFunctionBean(Annotation node) {
		ASTNode parent = node.getParent();
		if (parent instanceof MethodDeclaration) {
			MethodDeclaration method = (MethodDeclaration) parent;

			String returnType = null;
			if (method.getReturnType2().isParameterizedType()) {
				ParameterizedType paramType = (ParameterizedType) method.getReturnType2();
				Type type = paramType.getType();
				ITypeBinding typeBinding = type.resolveBinding();
				returnType = typeBinding.getBinaryName();
			}
			else {
				returnType = method.getReturnType2().resolveBinding().getQualifiedName();
			}

			return FUNCTION_FUNCTION_TYPE.equals(returnType) || FUNCTION_CONSUMER_TYPE.equals(returnType)
					|| FUNCTION_SUPPLIER_TYPE.equals(returnType);
		}
		return false;
	}

	private SymbolInformation createFunctionSymbol(Annotation node, TextDocument doc) throws BadLocationException {
		StringBuilder symbolLabel = new StringBuilder();
		symbolLabel.append("@> ");

		String beanName = getBeanName(node);
		String beanType = getBeanType(node);

		symbolLabel.append('\'');
		symbolLabel.append(beanName);
		symbolLabel.append('\'');
		symbolLabel.append(" (@Bean) ");
		symbolLabel.append(beanType);

		SymbolInformation symbol = new SymbolInformation(symbolLabel.toString(), SymbolKind.Interface,
				new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength())));
		return symbol;
	}

	private SymbolInformation createRegularBeanSymbol(Annotation node, TextDocument doc) throws BadLocationException {
		StringBuilder symbolLabel = new StringBuilder();
		symbolLabel.append("@+ ");

		String beanName = getBeanName(node);
		String beanType = getBeanType(node);

		symbolLabel.append('\'');
		symbolLabel.append(beanName);
		symbolLabel.append('\'');
		symbolLabel.append(" (@Bean) ");
		symbolLabel.append(beanType);

		SymbolInformation symbol = new SymbolInformation(symbolLabel.toString(), SymbolKind.Interface,
				new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength())));
		return symbol;
	}

	private String getBeanName(Annotation node) {
		ASTNode parent = node.getParent();
		if (parent instanceof MethodDeclaration) {
			MethodDeclaration method = (MethodDeclaration) parent;
			return method.getName().toString();
		}
		return null;
	}

	private String getBeanType(Annotation node) {
		ASTNode parent = node.getParent();
		if (parent instanceof MethodDeclaration) {
			MethodDeclaration method = (MethodDeclaration) parent;
			String returnType = method.getReturnType2().resolveBinding().getName();
			return returnType;
		}
		return null;
	}
}
