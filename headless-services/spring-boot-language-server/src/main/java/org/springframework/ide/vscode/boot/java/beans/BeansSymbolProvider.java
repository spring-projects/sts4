/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
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
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.boot.java.utils.FunctionUtils;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author Martin Lippert
 * @author Kris De Volder
 */
public class BeansSymbolProvider implements SymbolProvider {

	private static final String[] NAME_ATTRIBUTES = {"value", "name"};

	@Override
	public Collection<EnhancedSymbolInformation> getSymbols(Annotation node, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, TextDocument doc) {
		if (isMethodAbstract(node)) return null;

		ImmutableList.Builder<EnhancedSymbolInformation> symbols = ImmutableList.builder();

		boolean isFunction = isFunctionBean(node);
		String beanType = getBeanType(node);
		for (Tuple2<String, DocumentRegion> nameAndRegion : getBeanNames(node, doc)) {
			try {
				symbols.add(new EnhancedSymbolInformation(
						new SymbolInformation(
								beanLabel(isFunction, nameAndRegion.getT1(), beanType, "@Bean"),
								SymbolKind.Interface,
								new Location(doc.getUri(), doc.toRange(nameAndRegion.getT2()))),
						null
				));
			} catch (BadLocationException e) {
				Log.log(e);
			}
		}
		return symbols.build();
	}

	@Override
	public Collection<EnhancedSymbolInformation> getSymbols(TypeDeclaration typeDeclaration, TextDocument doc) {
		// this checks function beans that are defined as implementations of Function interfaces
		Tuple3<String, String, DocumentRegion> functionBean = FunctionUtils.getFunctionBean(typeDeclaration, doc);
		if (functionBean != null) {
			try {
				SymbolInformation symbol = new SymbolInformation(
						beanLabel(true, functionBean.getT1(), functionBean.getT2(), null),
						SymbolKind.Interface,
						new Location(doc.getUri(), doc.toRange(functionBean.getT3())));
				return ImmutableList.of(new EnhancedSymbolInformation(symbol, null));
			} catch (BadLocationException e) {
				Log.log(e);
			}
		}
		return ImmutableList.of();
	}

	protected Collection<Tuple2<String, DocumentRegion>> getBeanNames(Annotation node, TextDocument doc) {
		Collection<StringLiteral> beanNameNodes = getBeanNameLiterals(node);

		if (beanNameNodes != null && !beanNameNodes.isEmpty()) {
			ImmutableList.Builder<Tuple2<String,DocumentRegion>> namesAndRegions = ImmutableList.builder();
			for (StringLiteral nameNode : beanNameNodes) {
				String name = ASTUtils.getLiteralValue(nameNode);
				namesAndRegions.add(Tuples.of(name, ASTUtils.stringRegion(doc, nameNode)));
			}
			return namesAndRegions.build();
		}
		else {
			ASTNode parent = node.getParent();
			if (parent instanceof MethodDeclaration) {
				MethodDeclaration method = (MethodDeclaration) parent;
				return ImmutableList.of(Tuples.of(
						method.getName().toString(),
						ASTUtils.nameRegion(doc, node)
				));
			}
			return ImmutableList.of();
		}
	}

	protected String beanLabel(boolean isFunctionBean, String beanName, String beanType, String markerString) {
		StringBuilder symbolLabel = new StringBuilder();
		symbolLabel.append('@');
		symbolLabel.append(isFunctionBean ? '>' : '+');
		symbolLabel.append(' ');
		symbolLabel.append('\'');
		symbolLabel.append(beanName);
		symbolLabel.append('\'');

		markerString = markerString != null && markerString.length() > 0 ? " (" + markerString + ") " : " ";
		symbolLabel.append(markerString);

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

	protected String getBeanType(Annotation node) {
		ASTNode parent = node.getParent();
		if (parent instanceof MethodDeclaration) {
			MethodDeclaration method = (MethodDeclaration) parent;
			String returnType = method.getReturnType2().resolveBinding().getName();
			return returnType;
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

			return FunctionUtils.FUNCTION_FUNCTION_TYPE.equals(returnType) || FunctionUtils.FUNCTION_CONSUMER_TYPE.equals(returnType)
					|| FunctionUtils.FUNCTION_SUPPLIER_TYPE.equals(returnType);
		}
		return false;
	}

	private boolean isMethodAbstract(Annotation node) {
		if (node != null && node.getParent() != null && node.getParent() instanceof MethodDeclaration) {
			MethodDeclaration method = (MethodDeclaration) node.getParent();
			List<?> modifiers = method.modifiers();
			for (Object modifier : modifiers) {
				if (modifier instanceof Modifier && ((Modifier) modifier).isAbstract()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Collection<EnhancedSymbolInformation> getSymbols(MethodDeclaration methodDeclaration, TextDocument doc) {
		return null;
	}

}
