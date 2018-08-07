/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class RestrictedDefaultSymbolProvider implements SymbolProvider {

	@Override
	public Collection<EnhancedSymbolInformation> getSymbols(Annotation node, ITypeBinding typeBinding,
			Collection<ITypeBinding> metaAnnotations, TextDocument doc) {

		// provide default symbol only in case this annotation is not combined with @Bean annotation
		if (isCombinedWithAnnotation(node, Annotations.BEAN)) {
			return null;
		}
		else {
			ImmutableList.Builder<EnhancedSymbolInformation> symbols = ImmutableList.builder();
			try {
				symbols.add(new EnhancedSymbolInformation(DefaultSymbolProvider.provideDefaultSymbol(node, doc), null));
			} catch (Exception e) {
				Log.log(e);
			}
			return symbols.build();
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

	@Override
	public Collection<EnhancedSymbolInformation> getSymbols(TypeDeclaration typeDeclaration, TextDocument doc) {
		return null;
	}

	@Override
	public Collection<EnhancedSymbolInformation> getSymbols(MethodDeclaration methodDeclaration, TextDocument doc) {
		return null;
	}

}
