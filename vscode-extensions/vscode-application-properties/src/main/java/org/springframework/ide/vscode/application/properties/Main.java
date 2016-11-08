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
package org.springframework.ide.vscode.application.properties;

import java.io.IOException;
import java.util.logging.Logger;

import org.eclipse.lsp4j.services.LanguageServer;
import org.springframework.ide.vscode.application.properties.metadata.DefaultSpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtil;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.commons.languageserver.LaunguageServerApp;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;

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
			SpringPropertyIndexProvider indexProvider = new DefaultSpringPropertyIndexProvider(javaProjectFinder);
			TypeUtilProvider typeUtilProvider = (IDocument doc) -> new TypeUtil(javaProjectFinder.find(doc));
			LanguageServer server = new ApplicationPropertiesLanguageServer(indexProvider, typeUtilProvider);
			return server;
		});
	}

}
