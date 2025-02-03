/*******************************************************************************
 * Copyright (c) 2025 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.events;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.beans.CachedBean;
import org.springframework.ide.vscode.boot.java.handlers.AbstractSymbolProvider;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaContext;
import org.springframework.ide.vscode.commons.protocol.spring.AnnotationMetadata;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class EventListenerSymbolProvider extends AbstractSymbolProvider {
	
	private static final Logger log = LoggerFactory.getLogger(EventListenerSymbolProvider.class);

	@Override
	protected void addSymbolsPass1(Annotation node, ITypeBinding typeBinding, Collection<ITypeBinding> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
		if (node == null) return;
		
		ASTNode parent = node.getParent();
		if (parent == null || !(parent instanceof MethodDeclaration)) return;
		
		MethodDeclaration method = (MethodDeclaration) parent;
		
		
		// symbol
		try {
			String symbolLabel = createEventListenerSymbolLabel(node, method);
			WorkspaceSymbol symbol = new WorkspaceSymbol(symbolLabel, SymbolKind.Interface,
					Either.forLeft(new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength()))));

			EnhancedSymbolInformation enhancedSymbol = new EnhancedSymbolInformation(symbol);
			context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), enhancedSymbol));


			// index element for event listener
			Collection<Annotation> annotationsOnMethod = ASTUtils.getAnnotations(method);
			AnnotationMetadata[] annotations = ASTUtils.getAnnotationsMetadata(annotationsOnMethod, doc);

			List<CachedBean> beans = context.getBeans();
			if (beans.size() > 0 ) {

				CachedBean cachedBean = beans.get(beans.size() - 1);
				if (cachedBean.getDocURI().equals(doc.getUri())) {

					ITypeBinding eventType = getEventType(node, method);
					DocumentRegion nodeRegion = ASTUtils.nodeRegion(doc, method.getName());
					Location location = new Location(doc.getUri(), nodeRegion.asRange());
					
					String containerBeanType = null;
					TypeDeclaration type = ASTUtils.findDeclaringType(method);
					if (type != null) {
						ITypeBinding binding = type.resolveBinding();
						if (binding != null) {
							containerBeanType = binding.getQualifiedName();
						}
					}

					cachedBean.getBean().addChild(new EventListenerIndexElement(eventType != null ? eventType.getQualifiedName() : "", location, containerBeanType, annotations));
				}
			}
		} catch (BadLocationException e) {
			log.error("", e);
		}
	}
	
	@Override
	protected void addSymbolsPass1(TypeDeclaration typeDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
		super.addSymbolsPass1(typeDeclaration, context, doc);
	}
	
	private String createEventListenerSymbolLabel(Annotation node, MethodDeclaration method) {
		// event listener annotation type
		String annotationTypeName = getAnnotationTypeName(node);
		ITypeBinding eventType = getEventType(node, method);
		
		if (annotationTypeName != null) {
			return "@" + annotationTypeName + (eventType != null ? " (" + eventType.getName() + ")" : "");
		}
		else {
			return node.toString();
		}
	}
	
	private ITypeBinding getEventType(Annotation node, MethodDeclaration method) {
		List<?> parameters = method.parameters();
		if (parameters != null && parameters.size() == 1) {
			SingleVariableDeclaration param = (SingleVariableDeclaration) parameters.get(0);
			
			IVariableBinding paramBinding = param.resolveBinding();
			if (paramBinding != null) {
				ITypeBinding paramType = paramBinding.getType();
				return paramType != null ? paramType : null;
			}
		}

		return null;
	}

	private String getAnnotationTypeName(Annotation node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		if (typeBinding != null) {
			return typeBinding.getName();
		}
		else {
			return null;
		}
	}


}
