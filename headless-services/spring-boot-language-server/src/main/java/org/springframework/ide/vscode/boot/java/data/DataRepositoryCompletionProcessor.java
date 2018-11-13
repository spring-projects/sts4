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
package org.springframework.ide.vscode.boot.java.data;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.boot.java.handlers.CompletionProvider;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * @author Martin Lippert
 */
public class DataRepositoryCompletionProcessor implements CompletionProvider {

	@Override
	public void provideCompletions(ASTNode node, Annotation annotation, ITypeBinding type,
			int offset, IDocument doc, Collection<ICompletionProposal> completions) {
	}

	@Override
	public void provideCompletions(ASTNode node, int offset, IDocument doc, Collection<ICompletionProposal> completions) {
		TypeDeclaration type = ASTUtils.findDeclaringType(node);
		DataRepositoryDefinition repo = getDataRepositoryDefinition(type);
		if (repo != null) {
			DocumentEdits edits = new DocumentEdits(null);
			edits.insert(offset, "List<Customer> findByLastName${1|(String lastName);,And,Or|}");

			DocumentEdits additionalEdits = new DocumentEdits(null);
//			additionalEdits.insert(offset, "(String lastName);");

			completions.add(new FindByCompletionProposal("findByLastName(String lastName);", CompletionItemKind.Method, edits, null, null, Optional.of(additionalEdits)));

			System.out.println("data completion proposal calculation for: " + node.toString());
		}
	}

	private DataRepositoryDefinition getDataRepositoryDefinition(TypeDeclaration type) {
		if (type != null) {
			ITypeBinding resolvedType = type.resolveBinding();
			return getDataRepositoryDefinition(type, resolvedType);
		}

		return null;
	}

	private DataRepositoryDefinition getDataRepositoryDefinition(TypeDeclaration type, ITypeBinding resolvedType) {
		if (resolvedType != null) {

			// interface analysis
			ITypeBinding[] interfaces = resolvedType.getInterfaces();
			for (ITypeBinding resolvedInterface : interfaces) {
				String simplifiedType = null;
				if (resolvedInterface.isParameterizedType()) {
					simplifiedType = resolvedInterface.getBinaryName();
				}
				else {
					simplifiedType = resolvedType.getQualifiedName();
				}

				if (Constants.REPOSITORY_TYPE.equals(simplifiedType)) {
					return new DataRepositoryDefinition();
				}
				else {
					DataRepositoryDefinition repo = getDataRepositoryDefinition(type, resolvedInterface);
					if (repo != null) {
						return repo;
					}
				}
			}

			// super type analysis
			ITypeBinding superclass = resolvedType.getSuperclass();
			if (superclass != null) {
				return getDataRepositoryDefinition(type, superclass);
			}
		}
		return null;
	}
}
