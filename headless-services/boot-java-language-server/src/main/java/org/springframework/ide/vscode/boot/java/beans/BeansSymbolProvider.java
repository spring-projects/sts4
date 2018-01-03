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
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentRegion;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
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

	private static final String FUNCTION_FUNCTION_TYPE = Function.class.getName();
	private static final String FUNCTION_CONSUMER_TYPE = Consumer.class.getName();
	private static final String FUNCTION_SUPPLIER_TYPE = Supplier.class.getName();

	private static final String[] NAME_ATTRIBUTES = {"value", "name"};

	@Override
	public Collection<SymbolInformation> getSymbols(Annotation node, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, TextDocument doc) {
		boolean isFunction = isFunctionBean(node);

		ImmutableList.Builder<SymbolInformation> symbols = ImmutableList.builder();
		String beanType = getBeanType(node);
		for (Tuple2<String, DocumentRegion> nameAndRegion : getBeanNames(node, doc)) {
			try {
				symbols.add(new SymbolInformation(
						beanLabel(isFunction, nameAndRegion.getT1(), beanType, "@Bean"),
						SymbolKind.Interface,
						new Location(doc.getUri(), doc.toRange(nameAndRegion.getT2()))
				));
			} catch (BadLocationException e) {
				Log.log(e);
			}
		}
		return symbols.build();
	}

	@Override
	public Collection<SymbolInformation> getSymbols(TypeDeclaration typeDeclaration, TextDocument doc) {
		// this checks function beans that are defined as implementations of Function interfaces
		Tuple3<String, String, DocumentRegion> functionBean = getFunctionBean(typeDeclaration, doc);
		if (functionBean != null) {
			try {
				SymbolInformation symbol = new SymbolInformation(
						beanLabel(true, functionBean.getT1(), functionBean.getT2(), null),
						SymbolKind.Interface,
						new Location(doc.getUri(), doc.toRange(functionBean.getT3())));
				return ImmutableList.of(symbol);
			} catch (BadLocationException e) {
				Log.log(e);
			}
		}
		return ImmutableList.of();
	}

	protected Tuple3<String, String, DocumentRegion> getFunctionBean(TypeDeclaration typeDeclaration, TextDocument doc) {
		ITypeBinding resolvedType = typeDeclaration.resolveBinding();
		if (resolvedType != null) {
			return getFunctionBean(typeDeclaration, doc, resolvedType);
		}
		else {
			return null;
		}
	}

	private Tuple3<String, String, DocumentRegion> getFunctionBean(TypeDeclaration typeDeclaration, TextDocument doc,
			ITypeBinding resolvedType) {

		ITypeBinding[] interfaces = resolvedType.getInterfaces();
		for (ITypeBinding resolvedInterface : interfaces) {
			String simplifiedType = null;
			if (resolvedInterface.isParameterizedType()) {
				simplifiedType = resolvedInterface.getBinaryName();
			}
			else {
				simplifiedType = resolvedType.getQualifiedName();
			}

			if (FUNCTION_FUNCTION_TYPE.equals(simplifiedType) || FUNCTION_CONSUMER_TYPE.equals(simplifiedType)
					|| FUNCTION_SUPPLIER_TYPE.equals(simplifiedType)) {
				String beanName = getBeanName(typeDeclaration);
				String beanType = resolvedInterface.getName();
				DocumentRegion region = ASTUtils.nodeRegion(doc, typeDeclaration.getName());

				return Tuples.of(beanName, beanType, region);
			}
			else {
				Tuple3<String, String, DocumentRegion> result = getFunctionBean(typeDeclaration, doc, resolvedInterface);
				if (result != null) {
					return result;
				}
			}
		}

		ITypeBinding superclass = resolvedType.getSuperclass();
		if (superclass != null) {
			return getFunctionBean(typeDeclaration, doc, superclass);
		}
		else {
			return null;
		}
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

	protected String getBeanName(TypeDeclaration typeDeclaration) {
		String beanName = typeDeclaration.getName().toString();
		if (beanName.length() > 0 && Character.isUpperCase(beanName.charAt(0))) {
			beanName = Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
		}
		return beanName;
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


}
