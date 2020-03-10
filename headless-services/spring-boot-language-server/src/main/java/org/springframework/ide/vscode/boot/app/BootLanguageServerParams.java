/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.jdt.ls.JavaProjectsService;
import org.springframework.ide.vscode.boot.metadata.DefaultSpringPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.ValueProviderRegistry;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtil;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * Parameters for creating Boot Properties language server
 *
 * @author Alex Boyko
 * @author Kris De Volder
 */
public class BootLanguageServerParams {

	//TODO: This class is supposed to go away. It is basically a 'collection of beans'.
	// I.e. all the 'components' in here should really become separate beans.

	// So... moving forward...
	// Do not add more components here. You should instead just make your new
	// components into separate beans.

	//Shared
	public final JavaProjectFinder projectFinder;
	public final ProjectObserver projectObserver;
	public final SpringPropertyIndexProvider indexProvider;

	//Boot Properies
	public final TypeUtilProvider typeUtilProvider;

	public BootLanguageServerParams(
			JavaProjectFinder projectFinder,
			ProjectObserver projectObserver,
			SpringPropertyIndexProvider indexProvider,
			TypeUtilProvider typeUtilProvider
	) {
		super();
		Assert.isNotNull(projectObserver); // null is bad should be ProjectObserver.NULL
		this.projectFinder = projectFinder;
		this.projectObserver = projectObserver;
		this.indexProvider = indexProvider;
		this.typeUtilProvider = typeUtilProvider;
	}

	public static BootLanguageServerParams createDefault(SimpleLanguageServer server, ValueProviderRegistry valueProviders, JavaProjectsService javaProjectService) {
		// Initialize project finders, project caches and project observers
		FileObserver fileObserver = server.getWorkspaceService().getFileObserver();
		DefaultSpringPropertyIndexProvider indexProvider = new DefaultSpringPropertyIndexProvider(javaProjectService, javaProjectService, fileObserver, valueProviders);
		indexProvider.setProgressService(server.getProgressService());

		return new BootLanguageServerParams(
				javaProjectService.filter(project -> SpringProjectUtil.isBootProject(project) || SpringProjectUtil.isSpringProject(project)),
				javaProjectService,
				indexProvider,
				(SourceLinks sourceLinks, IDocument doc) -> new TypeUtil(sourceLinks, javaProjectService.find(new TextDocumentIdentifier(doc.getUri())))
		);
	}

}
