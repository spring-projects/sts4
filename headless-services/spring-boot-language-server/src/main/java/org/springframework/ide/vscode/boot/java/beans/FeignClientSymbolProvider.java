/*******************************************************************************
 * Copyright (c) 2023, 2024 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Tuple;
import org.eclipse.lsp4j.jsonrpc.messages.Tuple.Two;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.handlers.AbstractSymbolProvider;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaContext;
import org.springframework.ide.vscode.commons.protocol.spring.AnnotationMetadata;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.protocol.spring.InjectionPoint;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class FeignClientSymbolProvider extends AbstractSymbolProvider {
	
	private static final Logger log = LoggerFactory.getLogger(FeignClientSymbolProvider.class);

	@Override
	protected void addSymbolsPass1(Annotation node, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations,
			SpringIndexerJavaContext context, TextDocument doc) {
		try {
			if (node != null && node.getParent() != null && node.getParent() instanceof TypeDeclaration) {
				Two<EnhancedSymbolInformation, Bean> result = createSymbol(node, annotationType, metaAnnotations, doc);

				EnhancedSymbolInformation enhancedSymbol = result.getFirst();
				Bean beanDefinition = result.getSecond();
				context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), enhancedSymbol));
				context.getBeans().add(new CachedBean(context.getDocURI(), beanDefinition));
			}
		}
		catch (Exception e) {
			log.error("", e);
		}
	}

	private Two<EnhancedSymbolInformation, Bean> createSymbol(Annotation node, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, TextDocument doc) throws BadLocationException {
		String annotationTypeName = annotationType.getName();
		Collection<String> metaAnnotationNames = metaAnnotations.stream()
				.map(ITypeBinding::getName)
				.collect(Collectors.toList());
		
		TypeDeclaration type = (TypeDeclaration) node.getParent();

		String beanName = getBeanName(node, type);
		ITypeBinding beanType = type.resolveBinding();

		Location location = new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength()));
		
		WorkspaceSymbol symbol = new WorkspaceSymbol(
				beanLabel("+", annotationTypeName, metaAnnotationNames, beanName, beanType == null ? "" : beanType.getName()), SymbolKind.Interface,
				Either.forLeft(location));
		
		SymbolAddOnInformation[] addon = new SymbolAddOnInformation[] {new FeignClientBeanSymbolAddOnInformation(beanName, beanType == null ? "" : beanType.getQualifiedName(), getConfigClass(node))};
		
		InjectionPoint[] injectionPoints = ASTUtils.findInjectionPoints(type, doc);
		
		Set<String> supertypes = new HashSet<>();
		ASTUtils.findSupertypes(beanType, supertypes);
		
		Collection<Annotation> annotationsOnType = ASTUtils.getAnnotations(type);
		
		AnnotationMetadata[] annotations = Stream.concat(
				annotationsOnType.stream()
				.map(an -> an.resolveAnnotationBinding())
				.map(t -> new AnnotationMetadata(t.getAnnotationType().getQualifiedName(), false, ASTUtils.getAttributes(t)))
				,
				metaAnnotations.stream()
				.map(an -> new AnnotationMetadata(an.getQualifiedName(), true, null)))
				.toArray(AnnotationMetadata[]::new);
		
		Bean beanDefinition = new Bean(beanName, beanType == null ? "" : beanType.getQualifiedName(), location, injectionPoints, supertypes, annotations);

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

	private String getBeanName(Annotation node, TypeDeclaration typeDecl) {
		if (node.isSingleMemberAnnotation()) {
			Object o = ((SingleMemberAnnotation)node).getValue().resolveConstantExpressionValue();
			if (o instanceof String) {
				return (String) o;
			}
		} else if (node.isNormalAnnotation()) {
			NormalAnnotation normalAnnotation = (NormalAnnotation) node;
			for (Object o : normalAnnotation.values()) {
				if (o instanceof MemberValuePair) {
					MemberValuePair pair = (MemberValuePair) o;
					switch (pair.getName().getIdentifier()) {
					case "name":
					case "value":
						Object obj = pair.getValue().resolveConstantExpressionValue();
						if (obj instanceof String) {
							return (String) obj;
						}
					}
				}
			}
		}
		return BeanUtils.getBeanNameFromType(typeDecl.getName().getIdentifier());
	}
	
	private String[] getConfigClass(Annotation node) {
		if (node.isNormalAnnotation()) {
			NormalAnnotation normalAnnotation = (NormalAnnotation) node;
			for (Object o : normalAnnotation.values()) {
				if (o instanceof MemberValuePair) {
					MemberValuePair pair = (MemberValuePair) o;
					if ("configuration".equals(pair.getName().getIdentifier())) {
						if (pair.getValue() instanceof TypeLiteral) {
							ITypeBinding b = ((TypeLiteral) pair.getValue()).getType().resolveBinding();
							return new String[] { b.getQualifiedName() };
						} else if (pair.getValue() instanceof ArrayInitializer){
							List<?> expressions = (List<?>) ((ArrayInitializer) pair.getValue()).expressions();
							return expressions.stream()
								.filter(TypeLiteral.class::isInstance)
								.map(TypeLiteral.class::cast)
								.map(tl -> tl.getType().resolveBinding())
								.filter(Objects::nonNull)
								.map(b -> b.getQualifiedName())
								.toArray(String[]::new);
						}
					}
				}
			}
		}
		return new String[0];
	}

}
