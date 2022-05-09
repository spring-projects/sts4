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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.openrewrite.java.spring.NoRequestMappingAnnotation;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.rewrite.ORCompilationUnitCache;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRefactorings;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class NoRequestMappings extends AbstractRewriteJavaCodeAction {
	
	private static final String CODE_ACTION_ID = "RemoveAllRequestMappings";

	public NoRequestMappings(SimpleLanguageServer server, JavaProjectFinder projectFinder,
			RewriteRefactorings rewriteRefactorings, ORCompilationUnitCache orCuCache) {
		this(server, projectFinder, rewriteRefactorings, orCuCache, CODE_ACTION_ID);
	}

	public NoRequestMappings(SimpleLanguageServer server, JavaProjectFinder projectFinder,
			RewriteRefactorings rewriteRefactorings, ORCompilationUnitCache orCuCache, String codeActionId) {
		super(server, projectFinder, rewriteRefactorings, orCuCache, codeActionId);
	}
	
	final protected Optional<MethodDeclaration> findAppropriateMethodDeclaration(ASTNode node) {
		for (; node != null && !(node instanceof Annotation); node = node.getParent()) {
			// nothing
		}
		if (node instanceof Annotation) {
			Annotation a = (Annotation) node;
			ITypeBinding type = a.resolveTypeBinding();
			if (type != null && Annotations.SPRING_REQUEST_MAPPING.equals(type.getQualifiedName())) {
				if (a.getParent() instanceof MethodDeclaration) {
					return Optional.of((MethodDeclaration) a.getParent());
				}
			}
								
		}
		return Optional.empty();
	}

	@Override
	public WorkspaceEdit perform(List<?> args) {
		return perform(args, () -> new NoRequestMappingAnnotation());
	}

	@Override
	protected List<Either<Command, CodeAction>> provideCodeActions(CodeActionContext context, TextDocument doc, IRegion region, IJavaProject project,
			CompilationUnit cu, ASTNode node) {
		return findAppropriateMethodDeclaration(node).map(m -> List.of(
				Either.<Command, CodeAction>forRight(createCodeAction(CodeActionKind.Refactor, "Replace @RequestMapping with @GetMapping etc. in file", List.of(doc.getId().getUri(), false))),
				Either.<Command, CodeAction>forRight(createCodeAction(CodeActionKind.Refactor, "Replace @RequestMapping with @GetMapping etc. in project", List.of(doc.getId().getUri(), true)))
		)).orElse(Collections.emptyList());
	}
	
	

}
