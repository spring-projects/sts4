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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class BeansSymbolProvider implements SymbolProvider {

	private static final String FUNCTION_FUNCTION_TYPE = Function.class.getName();
	private static final String FUNCTION_CONSUMER_TYPE = Consumer.class.getName();
	private static final String FUNCTION_SUPPLIER_TYPE = Supplier.class.getName();
	private static final String[] NAME_ATTRIBUTES = {"value", "name"};

	@Override
	public Collection<SymbolInformation> getSymbols(Annotation node, TextDocument doc) {
		try {
			if (isFunctionBean(node)) {
				return Collections.singleton(createFunctionSymbol(node, doc));
			}
			else {
				return createRegularBeanSymbols(node, doc);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ImmutableList.of();
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

	private Collection<SymbolInformation> createRegularBeanSymbols(Annotation node, TextDocument doc) throws BadLocationException {

		Collection<StringLiteral> beanNameNodes = getBeanNameLiterals(node);
		if (beanNameNodes!=null && !beanNameNodes.isEmpty()) {
			ImmutableList.Builder<SymbolInformation> symbols = ImmutableList.builder();
			for (StringLiteral nameNode : beanNameNodes) {
				String name = ASTUtils.getLiteralValue(nameNode);
				String type = getBeanType(node);
				symbols.add(new SymbolInformation(
						beanLabel(name, type),
						SymbolKind.Interface,
						new Location(doc.getUri(), doc.toRange(ASTUtils.stringRegion(doc, nameNode)))
				));
			}
			return symbols.build();
		} else {
			return ImmutableList.of(new SymbolInformation(
					beanLabel(getBeanName(node), getBeanType(node)),
					SymbolKind.Interface,
					new Location(doc.getUri(), doc.toRange(ASTUtils.nameRegion(doc, node)))
			));
		}
	}

	protected String beanLabel(String beanName, String beanType) {
		StringBuilder symbolLabel = new StringBuilder();
		symbolLabel.append("@+ ");
		symbolLabel.append('\'');
		symbolLabel.append(beanName);
		symbolLabel.append('\'');
		symbolLabel.append(" (@Bean) ");
		symbolLabel.append(beanType);
		return symbolLabel.toString();
	}
	protected Collection<StringLiteral> getBeanNameLiterals(Annotation node) {
		ImmutableList.Builder<StringLiteral> literals = ImmutableList.builder();
		for (String attrib : NAME_ATTRIBUTES) {
			ASTUtils.getAttribute(node, attrib).ifPresent((valueExp) -> {
				literals.addAll(ASTUtils.getExpressionValueAsListOfLiterals(valueExp));
			});
		}
		return literals.build();
	}

	protected String getBeanName(Annotation node) {
		ASTNode parent = node.getParent();
		if (parent instanceof MethodDeclaration) {
			MethodDeclaration method = (MethodDeclaration) parent;
			return method.getName().toString();
		}
		return null;
	}

	protected String getBeanType(Annotation node) {
		ASTNode parent = node.getParent();
		if (parent instanceof MethodDeclaration) {
			MethodDeclaration method = (MethodDeclaration) parent;
			String returnType = method.getReturnType2().resolveBinding().getName();
			return returnType;
		}
		return null;
	}
}
