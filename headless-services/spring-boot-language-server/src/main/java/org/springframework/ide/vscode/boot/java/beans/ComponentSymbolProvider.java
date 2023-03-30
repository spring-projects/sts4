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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.index.InjectionPoint;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.handlers.AbstractSymbolProvider;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaContext;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 * @author Kris De Volder
 */
public class ComponentSymbolProvider extends AbstractSymbolProvider {
	
	private static final Logger log = LoggerFactory.getLogger(ComponentSymbolProvider.class);

	private final SpringMetamodelIndex springIndex;

	public ComponentSymbolProvider(SpringMetamodelIndex springIndex) {
		this.springIndex = springIndex;
	}

	@Override
	protected void addSymbolsPass1(Annotation node, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
		try {
			if (!isOnAnnotationDeclaration(node)) {
				EnhancedSymbolInformation enhancedSymbol = createSymbol(node, annotationType, metaAnnotations, doc);
				context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), enhancedSymbol));
			}
		}
		catch (Exception e) {
			log.error("", e);
		}
	}

	protected EnhancedSymbolInformation createSymbol(Annotation node, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, TextDocument doc) throws BadLocationException {
		String annotationTypeName = annotationType.getName();
		Collection<String> metaAnnotationNames = metaAnnotations.stream()
				.map(ITypeBinding::getName)
				.collect(Collectors.toList());
		String beanName = getBeanName(node);
		ITypeBinding beanType = getBeanType(node);

		Location location = new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength()));
		
		WorkspaceSymbol symbol = new WorkspaceSymbol(
				beanLabel("+", annotationTypeName, metaAnnotationNames, beanName, beanType.getName()), SymbolKind.Interface,
				Either.forLeft(location));
		
		SymbolAddOnInformation[] addon = new SymbolAddOnInformation[0];
		if (Annotations.CONFIGURATION.equals(annotationType.getQualifiedName())
				|| metaAnnotations.stream().anyMatch(t -> Annotations.CONFIGURATION.equals(t.getQualifiedName()))) {
			addon = new SymbolAddOnInformation[] {new ConfigBeanSymbolAddOnInformation(beanName, beanType.getQualifiedName())};
		} else {
			addon = new SymbolAddOnInformation[] {new BeansSymbolAddOnInformation(beanName, beanType.getQualifiedName())};
		}
		
		InjectionPoint[] injectionPoints = findInjectionPoints(node, doc);
		springIndex.registerBean(beanName, beanType.getQualifiedName(), location, injectionPoints);

		return new EnhancedSymbolInformation(symbol, addon);
	}

	private InjectionPoint[] findInjectionPoints(Annotation node, TextDocument doc) throws BadLocationException {
		List<InjectionPoint> result = new ArrayList<>();

		ASTNode parent = node.getParent();
		if (parent instanceof TypeDeclaration) {
			TypeDeclaration type = (TypeDeclaration) parent;
			
			MethodDeclaration[] methods = type.getMethods();
			for (MethodDeclaration method : methods) {
				if (method.isConstructor()) {
					result.addAll(ASTUtils.getInjectionPointsFromMethodParams(method, doc));
				}
			}
			
			FieldDeclaration[] fields = type.getFields();
			for (FieldDeclaration field : fields) {
				
				boolean autowiredField = false;
				
				List<?> modifiers = field.modifiers();
				for (Object modifier : modifiers) {
					if (modifier instanceof Annotation) {
						Annotation annotation = (Annotation) modifier;
						
						String qualifiedName = annotation.resolveTypeBinding().getQualifiedName();
						if (Annotations.AUTOWIRED.equals(qualifiedName)) {
							autowiredField = true;
						}
					}
				}
				

				if (autowiredField) {
					List<?> fragments = field.fragments();
					for (Object fragment : fragments) {
						if (fragment instanceof VariableDeclarationFragment) {
							VariableDeclarationFragment varFragment = (VariableDeclarationFragment) fragment;
							String fieldName = varFragment.getName().toString();
							
							DocumentRegion region = ASTUtils.nodeRegion(doc, varFragment.getName());
							Range range = doc.toRange(region);
							Location fieldLocation = new Location(doc.getUri(), range);
	
							String fieldType = field.getType().resolveBinding().getQualifiedName();
							
							result.add(new InjectionPoint(fieldName, fieldType, fieldLocation));
						}
					}
				}
			}
		}
		
		return (InjectionPoint[]) result.toArray(new InjectionPoint[result.size()]);
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

	private String getBeanName(Annotation node) {
		ASTNode parent = node.getParent();
		if (parent instanceof TypeDeclaration) {
			TypeDeclaration type = (TypeDeclaration) parent;

			String beanName = type.getName().toString();
			return BeanUtils.getBeanNameFromType(beanName);
		}
		return null;
	}

	private ITypeBinding getBeanType(Annotation node) {
		ASTNode parent = node.getParent();
		if (parent instanceof TypeDeclaration) {
			TypeDeclaration type = (TypeDeclaration) parent;
			return type.resolveBinding();
		}
		return null;
	}
	
	private boolean isOnAnnotationDeclaration(Annotation node) {
		ASTNode parent = node.getParent();
		if (parent != null && parent instanceof AnnotationTypeDeclaration) {
			return true;
		}
		return false;
	}



}
