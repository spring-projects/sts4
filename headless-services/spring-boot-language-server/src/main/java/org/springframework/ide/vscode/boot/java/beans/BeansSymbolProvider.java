/*******************************************************************************
 * Copyright (c) 2017, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans;

import java.util.Collection;
import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.Literal;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.J.Modifier;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.JavaType.Method;
import org.openrewrite.java.tree.JavaType.Parameterized;
import org.openrewrite.java.tree.TypeUtils;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.handlers.AbstractSymbolProvider;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;
import org.springframework.ide.vscode.boot.java.utils.FunctionUtils;
import org.springframework.ide.vscode.boot.java.utils.ORAstUtils;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaContext;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.Parameter;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author Martin Lippert
 * @author Kris De Volder
 */
public class BeansSymbolProvider extends AbstractSymbolProvider {

	private static final String[] NAME_ATTRIBUTES = {"value", "name"};

	@Override
	protected void addSymbolsPass1(Annotation node, FullyQualified annotationType, Collection<FullyQualified> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
		if (isMethodAbstract(node)) return;

		boolean isFunction = isFunctionBean(node);
		String beanType = getBeanType(node);
		String markerString = getAnnotations(node);
		for (Tuple2<String, DocumentRegion> nameAndRegion : getBeanNames(node, doc)) {
			try {
				EnhancedSymbolInformation enhancedSymbol = new EnhancedSymbolInformation(
						new SymbolInformation(
								beanLabel(isFunction, nameAndRegion.getT1(), beanType, "@Bean" + markerString),
								SymbolKind.Interface,
								new Location(doc.getUri(), doc.toRange(nameAndRegion.getT2()))),
						new SymbolAddOnInformation[] {new BeansSymbolAddOnInformation(nameAndRegion.getT1())}
				);

				context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), enhancedSymbol));

			} catch (BadLocationException e) {
				Log.log(e);
			}
		}
	}

	@Override
	protected void addSymbolsPass1(ClassDeclaration typeDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
		// this checks function beans that are defined as implementations of Function interfaces
		Tuple3<String, String, DocumentRegion> functionBean = FunctionUtils.getFunctionBean(typeDeclaration, doc);
		if (functionBean != null) {
			try {
				SymbolInformation symbol = new SymbolInformation(
						beanLabel(true, functionBean.getT1(), functionBean.getT2(), null),
						SymbolKind.Interface,
						new Location(doc.getUri(), doc.toRange(functionBean.getT3())));

				context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(),
						new EnhancedSymbolInformation(symbol, new SymbolAddOnInformation[] {new BeansSymbolAddOnInformation(functionBean.getT1())})));

			} catch (BadLocationException e) {
				Log.log(e);
			}
		}
	}

	protected Collection<Tuple2<String, DocumentRegion>> getBeanNames(Annotation node, TextDocument doc) {
		Collection<Literal> beanNameNodes = getBeanNameLiterals(node);

		if (beanNameNodes != null && !beanNameNodes.isEmpty()) {
			ImmutableList.Builder<Tuple2<String,DocumentRegion>> namesAndRegions = ImmutableList.builder();
			for (Literal nameNode : beanNameNodes) {
				String name = ORAstUtils.getLiteralValue(nameNode);
				namesAndRegions.add(Tuples.of(name, ORAstUtils.stringRegion(doc, nameNode)));
			}
			return namesAndRegions.build();
		}
		else {
			J parent = ORAstUtils.getParent(node);
			if (parent instanceof MethodDeclaration) {
				MethodDeclaration method = (MethodDeclaration) parent;
				return ImmutableList.of(Tuples.of(
						method.getName().toString(),
						ORAstUtils.nameRegion(doc, node)
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

	protected Collection<Literal> getBeanNameLiterals(Annotation node) {
		ImmutableList.Builder<Literal> literals = ImmutableList.builder();
		for (String attrib : NAME_ATTRIBUTES) {
			ORAstUtils.getAttribute(node, attrib).ifPresent((valueExp) -> {
				literals.addAll(ORAstUtils.getExpressionValueAsListOfLiterals(valueExp));
			});
		}
		return literals.build();
	}

	protected String getBeanType(Annotation node) {
		J parent = ORAstUtils.getParent(node);
		if (parent instanceof MethodDeclaration) {
			Method method = ((MethodDeclaration) parent).getMethodType();
			if (method != null) {
				FullyQualified returnType = TypeUtils.asFullyQualified(method.getReturnType());
				if (returnType != null) {
					return returnType.getFullyQualifiedName();
				}
			}
		}
		return null;
	}

	private boolean isFunctionBean(Annotation node) {
		J parent = ORAstUtils.getParent(node);
		if (parent instanceof MethodDeclaration) {
			Method method = ((MethodDeclaration) parent).getMethodType();
			if (method != null) {
				FullyQualified returnType = TypeUtils.asFullyQualified(method.getReturnType());
				if (returnType != null) {
					String fqName = returnType.getFullyQualifiedName();
					
					return FunctionUtils.FUNCTION_FUNCTION_TYPE.equals(fqName) || FunctionUtils.FUNCTION_CONSUMER_TYPE.equals(fqName)
							|| FunctionUtils.FUNCTION_SUPPLIER_TYPE.equals(fqName);
				}
			}
		}
		return false;
	}

	private String getAnnotations(Annotation node) {
		StringBuilder result = new StringBuilder();
		J parent = ORAstUtils.getParent(node);
		if (parent instanceof MethodDeclaration) {
			MethodDeclaration method = (MethodDeclaration) parent;
			
			for (Annotation a : method.getLeadingAnnotations()) {
				FullyQualified type = TypeUtils.asFullyQualified(a.getType());
				if (type != null && !Annotations.BEAN.equals(type.getFullyQualifiedName())) {
					result.append(' ');
					result.append(a.printTrimmed());
				}
			}
			
		}
		return result.toString();
	}

	private boolean isMethodAbstract(Annotation node) {
		if (node != null) {
			J parent = ORAstUtils.getParent(node);
			if (parent instanceof MethodDeclaration) {
				MethodDeclaration method = (MethodDeclaration) parent;
				return Modifier.hasModifier(method.getModifiers(), Modifier.Type.Abstract);
			}
		}
		return false;
	}

}
