/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.handlers.AbstractSymbolProvider;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class RestrictedDefaultSymbolProvider extends AbstractSymbolProvider {

	private static final Logger log = LoggerFactory.getLogger(RestrictedDefaultSymbolProvider.class);

	@Override
	protected void addSymbolsPass1(Annotation node, ITypeBinding typeBinding,
			Collection<ITypeBinding> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {

		// provide default symbol only in case this annotation is not combined with @Bean annotation
		if (!isCombinedWithAnnotation(node, Annotations.BEAN)) {
			try {
				EnhancedSymbolInformation enhancedSymbol = new EnhancedSymbolInformation(DefaultSymbolProvider.provideDefaultSymbol(node, doc), null);
				context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), enhancedSymbol));
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
	}

	private boolean isCombinedWithAnnotation(Annotation node, String annotation) {
		ASTNode parent = node.getParent();

		if (parent instanceof MethodDeclaration) {
			MethodDeclaration method = (MethodDeclaration) parent;

			List<?> modifiers = method.modifiers();
			for (Object modifier : modifiers) {
				if (modifier instanceof Annotation) {
					Annotation anno = (Annotation) modifier;
					IAnnotationBinding annotationBinding = anno.resolveAnnotationBinding();
					String type = annotationBinding.getAnnotationType().getBinaryName();

					if (type != null && type.equals(annotation)) {
						return true;
					}
				}
			}
		}

		return false;
	}

}
