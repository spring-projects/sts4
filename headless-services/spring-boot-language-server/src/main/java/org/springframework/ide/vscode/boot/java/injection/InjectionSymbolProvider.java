/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gayan Perera <gayanper@gmail.com> - Symbol provider for injectable annotations
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.injection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

public class InjectionSymbolProvider implements SymbolProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(InjectionSymbolProvider.class);

	@Override
	public Collection<EnhancedSymbolInformation> getSymbols(Annotation node, ITypeBinding typeBinding,
			Collection<ITypeBinding> metaAnnotations, TextDocument doc) {
		if (node.getParent() != null) {
			try {
				switch (node.getParent().getNodeType()) {
				case ASTNode.FIELD_DECLARATION: {
					return createFieldSymbol(node, typeBinding, metaAnnotations, doc);
				}
				case ASTNode.METHOD_DECLARATION: {
					break;
				}
				}
			} catch (BadLocationException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		return null;
	}

	private Collection<EnhancedSymbolInformation> createFieldSymbol(Annotation node, ITypeBinding annotationType,
			Collection<ITypeBinding> metaAnnotations, TextDocument doc) throws BadLocationException {
		String annotationString = node.toString();

		FieldDeclaration fieldDeclaration = (FieldDeclaration) node.getParent();
		@SuppressWarnings("unchecked")
		List<VariableDeclarationFragment> fragments = fieldDeclaration.fragments();
		com.google.common.collect.ImmutableList.Builder<EnhancedSymbolInformation> builder = ImmutableList.builder();

		for (VariableDeclarationFragment fragment : fragments) {
			SymbolInformation symbol = new SymbolInformation(label(fragment.getName().getIdentifier(), annotationString,
					fieldDeclaration.getType().resolveBinding().getName(), getAnnotations(node, annotationType)),
					SymbolKind.Interface,
					new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength())));
			builder.add(new EnhancedSymbolInformation(symbol, null));
		}
		return builder.build();
	}

	private String label(String target, String annotationString, String fieldTypeName, String qualifierString) {
		StringBuilder symbolLabel = new StringBuilder();
		symbolLabel.append('@');
		symbolLabel.append('=');
		symbolLabel.append(' ');
		symbolLabel.append('\'');
		symbolLabel.append(target);
		symbolLabel.append('\'').append(' ');
		symbolLabel.append('(');
		symbolLabel.append(annotationString);
		if(!qualifierString.isEmpty()) {
			symbolLabel.append(' ');
			symbolLabel.append(qualifierString);
		}
		symbolLabel.append(')').append(' ');
		symbolLabel.append(fieldTypeName);
		return symbolLabel.toString();
	}

	private String getAnnotations(Annotation node, ITypeBinding annotationTypeBinding) {
		StringBuilder result = new StringBuilder();
		ASTNode parent = node.getParent();
		String annotationName = annotationTypeBinding.getBinaryName();

		List<Annotation> annotations = Collections.emptyList();
		if (parent instanceof MethodDeclaration) {
			annotations = annotationsFromMethod((MethodDeclaration) parent, annotationName);
		} else if (parent instanceof FieldDeclaration) {
			annotations = annotationsFromField((FieldDeclaration) parent, annotationName);
		}

		annotations.forEach(a -> {
			result.append(' ');
			result.append(a.toString());
		});
		return result.toString();
	}

	private List<Annotation> annotationsFromField(FieldDeclaration declaration, String injectAnnotation) {
		List<?> modifiers = declaration.modifiers();
		return extractAnnotations(injectAnnotation, modifiers);
	}

	private List<Annotation> annotationsFromMethod(MethodDeclaration declaration, String injectAnnotation) {
		List<?> modifiers = declaration.modifiers();
		return extractAnnotations(injectAnnotation, modifiers);
	}

	private List<Annotation> extractAnnotations(String injectAnnotation, List<?> modifiers) {
		return modifiers.stream().filter(m -> m instanceof Annotation).map(m -> (Annotation) m).filter(m -> {
			String type = m.resolveAnnotationBinding().getAnnotationType().getBinaryName();
			return (type != null && !injectAnnotation.equals(type));
		}).collect(Collectors.toList());
	}

	@Override
	public Collection<EnhancedSymbolInformation> getSymbols(TypeDeclaration typeDeclaration, TextDocument doc) {
		return null;
	}

	@Override
	public Collection<EnhancedSymbolInformation> getSymbols(MethodDeclaration methodDeclaration, TextDocument doc) {
		return null;
	}

}
