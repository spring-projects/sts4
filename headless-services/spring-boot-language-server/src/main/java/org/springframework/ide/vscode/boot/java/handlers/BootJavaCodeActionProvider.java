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
package org.springframework.ide.vscode.boot.java.handlers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.CodeActionHandler;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class BootJavaCodeActionProvider implements CodeActionHandler {
	
	final private JavaProjectFinder projectFinder;
	final private CompilationUnitCache cuCache;
	private Collection<JavaCodeAction> javaCodeActions;
	
	public BootJavaCodeActionProvider(JavaProjectFinder projectFinder, CompilationUnitCache cuCache, Collection<JavaCodeAction> javaCodeActions) {
		this.projectFinder = projectFinder;
		this.cuCache = cuCache;
		this.javaCodeActions = javaCodeActions;
	}

	@Override
	public List<Either<Command, CodeAction>> handle(CancelChecker cancelToken, CodeActionCapabilities capabilities, CodeActionContext context, TextDocument doc, IRegion region) {
		Optional<IJavaProject> project = projectFinder.find(doc.getId());
		if (project.isPresent()) {
			return cuCache.withCompilationUnit(project.get(), URI.create(doc.getId().getUri()), cu -> {
				ASTNode found = NodeFinder.perform(cu, region.getOffset(), region.getLength());
				List<Either<Command, CodeAction>> codeActions = new ArrayList<>();
				for (JavaCodeAction jca : javaCodeActions) {
					List<Either<Command, CodeAction>> cas = jca.getCodeActions(capabilities, context, doc, region, project.get(), cu, found);
					if (cas != null) {
						codeActions.addAll(cas);
					}
				}
				return codeActions;
			});
		}
		return Collections.emptyList();
	}
	
}
