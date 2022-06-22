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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.CodeActionHandler;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class BootJavaCodeActionProvider implements CodeActionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(BootJavaCodeActionProvider.class);
		
	final private JavaProjectFinder projectFinder;
	final private Collection<JavaCodeActionHandler> handlers;
	
	public BootJavaCodeActionProvider(JavaProjectFinder projectFinder, Collection<JavaCodeActionHandler> handlers) {
		this.projectFinder = projectFinder;
		this.handlers = handlers;
	}
	
	@Override
	public List<Either<Command, CodeAction>> handle(CancelChecker cancelToken, CodeActionCapabilities capabilities, CodeActionContext context, TextDocument doc, IRegion region) {
		Optional<IJavaProject> project = projectFinder.find(doc.getId());
		if (project.isPresent()) {
			return handlers.stream().flatMap(handler -> {
				try {
					return handler.handle(project.get(), cancelToken, capabilities, context, doc, region).stream();
				} catch (Exception e) {
					log.error("", e);
					return Stream.empty();
				}
			}).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
	
}
