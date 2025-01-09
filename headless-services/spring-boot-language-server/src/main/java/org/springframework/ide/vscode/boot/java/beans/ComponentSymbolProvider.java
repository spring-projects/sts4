/*******************************************************************************
 * Copyright (c) 2017, 2024 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Tuple;
import org.eclipse.lsp4j.jsonrpc.messages.Tuple.Two;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.handlers.AbstractSymbolProvider;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;
import org.springframework.ide.vscode.boot.java.utils.DefaultSymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaContext;
import org.springframework.ide.vscode.commons.protocol.spring.AnnotationMetadata;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.protocol.spring.InjectionPoint;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 * @author Kris De Volder
 */
public class ComponentSymbolProvider extends AbstractSymbolProvider {
	
	private static final Logger log = LoggerFactory.getLogger(ComponentSymbolProvider.class);

	@Override
	protected void addSymbolsPass1(Annotation node, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
		try {
			if (node != null && node.getParent() != null && node.getParent() instanceof TypeDeclaration) {
				Two<EnhancedSymbolInformation, Bean> result = createSymbol(node, annotationType, metaAnnotations, doc);

				EnhancedSymbolInformation enhancedSymbol = result.getFirst();
				Bean beanDefinition = result.getSecond();
				context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), enhancedSymbol));
				context.getBeans().add(new CachedBean(context.getDocURI(), beanDefinition));
			}
			else if (Annotations.NAMED_ANNOTATIONS.contains(annotationType.getQualifiedName())) {
				WorkspaceSymbol symbol = DefaultSymbolProvider.provideDefaultSymbol(node, doc);
				EnhancedSymbolInformation enhancedSymbol = new EnhancedSymbolInformation(symbol, null);
				context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), enhancedSymbol));
			}
		}
		catch (Exception e) {
			log.error("", e);
		}
	}

	protected Tuple.Two<EnhancedSymbolInformation, Bean> createSymbol(Annotation node, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, TextDocument doc) throws BadLocationException {
		String annotationTypeName = annotationType.getName();
		
		Collection<String> metaAnnotationNames = metaAnnotations.stream()
				.map(ITypeBinding::getName)
				.collect(Collectors.toList());
		
		TypeDeclaration type = (TypeDeclaration) node.getParent();

		String beanName = getBeanName(type);
		ITypeBinding beanType = getBeanType(type);

		Location location = new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength()));
		
		WorkspaceSymbol symbol = new WorkspaceSymbol(
				beanLabel("+", annotationTypeName, metaAnnotationNames, beanName, beanType.getName()), SymbolKind.Interface,
				Either.forLeft(location));
		
		boolean isConfiguration = Annotations.CONFIGURATION.equals(annotationType.getQualifiedName())
				|| metaAnnotations.stream().anyMatch(t -> Annotations.CONFIGURATION.equals(t.getQualifiedName()));

		SymbolAddOnInformation[] addon = new SymbolAddOnInformation[] {new BeansSymbolAddOnInformation(beanName, beanType.getQualifiedName())};
		
		InjectionPoint[] injectionPoints = ASTUtils.findInjectionPoints(type, doc);
		
		Set<String> supertypes = new HashSet<>();
		ASTUtils.findSupertypes(beanType, supertypes);
		
		Collection<Annotation> annotationsOnType = ASTUtils.getAnnotations(type);
		
		AnnotationMetadata[] annotations = Stream.concat(
				Arrays.stream(ASTUtils.getAnnotationsMetadata(annotationsOnType, doc))
				,
				metaAnnotations.stream()
				.map(an -> new AnnotationMetadata(an.getQualifiedName(), true, null, null)))
				.toArray(AnnotationMetadata[]::new);
		
		Bean beanDefinition = new Bean(beanName, beanType.getQualifiedName(), location, injectionPoints, supertypes, annotations, isConfiguration);

		return Tuple.two(new EnhancedSymbolInformation(symbol, addon), beanDefinition);
	}

	protected String beanLabel(String searchPrefix, String annotationTypeName, Collection<String> metaAnnotationNames, String beanName, String beanType) {
		StringBuilder symbolLabel = new StringBuilder();
		symbolLabel.append("@");
		symbolLabel.append(searchPrefix);
		symbolLabel.append(' ');
		symbolLabel.append('\'');
		symbolLabel.append(beanName);
		symbolLabel.append('\'');
		symbolLabel.append(" (@");
		symbolLabel.append(annotationTypeName);
		if (!metaAnnotationNames.isEmpty()) {
			symbolLabel.append(" <: ");
			boolean first = true;
			for (String ma : metaAnnotationNames) {
				if (!first) {
					symbolLabel.append(", ");
				}
				symbolLabel.append("@");
				symbolLabel.append(ma);
				first = false;
			}
		}
		symbolLabel.append(") ");
		symbolLabel.append(beanType);
		return symbolLabel.toString();
	}

	private String getBeanName(TypeDeclaration type) {
		String beanName = type.getName().toString();
		return BeanUtils.getBeanNameFromType(beanName);
	}

	private ITypeBinding getBeanType(TypeDeclaration type) {
		return type.resolveBinding();
	}
	
}
