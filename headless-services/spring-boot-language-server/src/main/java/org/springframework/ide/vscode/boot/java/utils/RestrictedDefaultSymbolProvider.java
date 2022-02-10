/*******************************************************************************
 * Copyright (c) 2018, 2022 Pivotal, Inc.
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

import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;
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
	protected void addSymbolsPass1(Annotation node, FullyQualified typeBinding,
			Collection<FullyQualified> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {

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
		J parent = ORAstUtils.getParent(node);

		if (parent instanceof MethodDeclaration) {
			MethodDeclaration method = (MethodDeclaration) parent;

			for (Annotation a : method.getLeadingAnnotations()) {
				FullyQualified otherAnnotationType = TypeUtils.asFullyQualified(a.getType());
				if (otherAnnotationType != null && annotation.equals(otherAnnotationType.getFullyQualifiedName())) {
					return true;
				}
			}
		}

		return false;
	}

}
