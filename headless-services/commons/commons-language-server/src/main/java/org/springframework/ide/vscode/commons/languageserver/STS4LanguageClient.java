/*******************************************************************************
 * Copyright (c) 2016, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.languageserver;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;
import org.springframework.ide.vscode.commons.languageserver.java.ls.ClasspathListenerParams;
import org.springframework.ide.vscode.commons.languageserver.java.ls.JavaDataParams;
import org.springframework.ide.vscode.commons.languageserver.java.ls.JavaTypeResponse;
import org.springframework.ide.vscode.commons.languageserver.java.ls.JavadocHoverLinkResponse;
import org.springframework.ide.vscode.commons.languageserver.java.ls.JavadocResponse;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixEdit.CursorMovement;

/**
 * Some 'custom' extensions to standard LSP {@link LanguageClient}.
 *
 * @author Kris De Volder
 */
public interface STS4LanguageClient extends LanguageClient {

	@JsonNotification("sts/highlight")
	void highlight(HighlightParams highlights);

	@JsonNotification("sts/progress")
	void progress(ProgressParams progressEvent);

	@JsonRequest("sts/moveCursor")
	CompletableFuture<Object> moveCursor(CursorMovement cursorMovement);

	@JsonRequest("sts/addClasspathListener")
	CompletableFuture<Object> addClasspathListener(ClasspathListenerParams params);

	@JsonRequest("sts/removeClasspathListener")
	CompletableFuture<Object> removeClasspathListener(ClasspathListenerParams classpathListenerParams);

	@JsonRequest("sts/javadoc")
	CompletableFuture<JavadocResponse> javadoc(JavaDataParams params);

	@JsonRequest("sts/javaType")
	CompletableFuture<JavaTypeResponse> javaType(JavaDataParams params);

	@JsonRequest("sts/javadocHoverLink")
	CompletableFuture<JavadocHoverLinkResponse> javadocHoverLink(JavaDataParams params);

}
