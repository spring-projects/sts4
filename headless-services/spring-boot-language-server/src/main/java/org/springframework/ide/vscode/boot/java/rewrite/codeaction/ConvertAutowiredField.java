/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.rewrite.codeaction;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.rewrite.ConvertAutowiredParameterIntoConstructorParameter;
import org.springframework.ide.vscode.boot.java.rewrite.ORCompilationUnitCache;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRefactorings;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class ConvertAutowiredField extends AbstractRewriteJavaCodeAction {

	private static final String CODE_ACTION_ID = "ConvertAutowiredParameterIntoConstructorParameter";

	public ConvertAutowiredField(SimpleLanguageServer server, JavaProjectFinder projectFinder,
			RewriteRefactorings rewriteRefactorings, ORCompilationUnitCache orCuCache) {
		super(server, projectFinder, rewriteRefactorings, orCuCache, CODE_ACTION_ID);
	}

	@Override
	public WorkspaceEdit perform(List<?> args) {
		String docUri = (String) args.get(0);
		String classFqName = (String) args.get(1);
		String fieldName = (String) args.get(2);

		Optional<IJavaProject> project = projectFinder.find(new TextDocumentIdentifier(docUri));

		if (project.isPresent()) {
			return orCuCache.withCompilationUnit(project.get(), URI.create(docUri), cu -> {
				if (cu == null) {
					throw new IllegalStateException("Cannot parse Java file: " + docUri);
				}
				return applyRecipe(new ConvertAutowiredParameterIntoConstructorParameter(classFqName, fieldName), project.get(), List.of(cu));
			});
		}
		return null;
	}

	@Override
	protected List<Either<Command, CodeAction>> provideCodeActions(CodeActionContext context, TextDocument doc, IRegion region, IJavaProject project,
			CompilationUnit cu, ASTNode node) {				
		for (; node != null && !(node instanceof FieldDeclaration); node = node.getParent()) {
			// nothing
		}
		if (node instanceof FieldDeclaration) {
			FieldDeclaration fd = (FieldDeclaration) node;

			if (fd.fragments().size() == 1) {
				@SuppressWarnings("unchecked")
				Optional<Annotation> autowired = fd.modifiers().stream().filter(Annotation.class::isInstance)
						.map(Annotation.class::cast).filter(a -> {
							IAnnotationBinding binding = ((Annotation) a).resolveAnnotationBinding();
							if (binding != null && binding.getAnnotationType() != null) {
								return Annotations.AUTOWIRED.equals(binding.getAnnotationType().getQualifiedName());
							}
							return false;
						}).findFirst();

				if (autowired.isPresent()) {

					return List.of(Either.forRight(createCodeAction("Convert into Constructor Parameter",
							List.of(doc.getId().getUri(),
									((TypeDeclaration) fd.getParent()).resolveBinding().getQualifiedName(),
									((VariableDeclarationFragment) fd.fragments().get(0)).getName().getIdentifier()))));
				}
			}

		}
		return Collections.emptyList();
	}

}
