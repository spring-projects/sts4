/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.FileObserver;

public class ServerUtils {
	
	private static final Logger log = LoggerFactory.getLogger(ServerUtils.class);

	private static final List<String> CLASS_FILES_TO_WATCH_GLOB = List.of("**/*.class");
		
	public static void listenToClassFileChanges(FileObserver fileObserver, JavaProjectFinder projectFinder, Consumer<IJavaProject> callback) {
		fileObserver.onFilesChanged(CLASS_FILES_TO_WATCH_GLOB, files -> handleFiles(projectFinder, files, callback));
		fileObserver.onFilesCreated(CLASS_FILES_TO_WATCH_GLOB, files -> handleFiles(projectFinder, files, callback));
	}
	
	private static void handleFiles(JavaProjectFinder projectFinder, String[] files, Consumer<IJavaProject> callback) {
		for (String f : files) {
			URI uri = URI.create(f);
			TextDocumentIdentifier docId = new TextDocumentIdentifier(uri.toASCIIString());
			projectFinder.find(docId).ifPresent(project -> {
				Path p = Paths.get(uri);
				if (IClasspathUtil.getOutputFolders(project.getClasspath()).anyMatch(folder -> p.startsWith(folder.toPath()))) {
					try {
						callback.accept(project);
					} catch (Throwable t) {
						log.error("", t);
					}
				}
			});
		}
	}


}
