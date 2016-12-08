/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot;

import java.io.IOException;

import org.springframework.ide.vscode.application.properties.metadata.DefaultSpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtil;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.commons.languageserver.LaunguageServerApp;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

/**
 * Starts up Language Server process
 * 
 * @author Alex Boyko
 * @author Kris De Volder
 *
 */
public class Main {
	
	public static void main(String[] args) throws IOException {
		LaunguageServerApp.start(() -> {
			JavaProjectFinder javaProjectFinder = JavaProjectFinder.DEFAULT;
			DefaultSpringPropertyIndexProvider indexProvider = new DefaultSpringPropertyIndexProvider(javaProjectFinder);
			TypeUtilProvider typeUtilProvider = (IDocument doc) -> new TypeUtil(javaProjectFinder.find(doc));
			SimpleLanguageServer server = new BootPropertiesLanguageServer(indexProvider, typeUtilProvider, javaProjectFinder);
			indexProvider.setProgressService(server.getProgressService());
			return server;
		});
	}

}
