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

import org.springframework.ide.vscode.boot.metadata.DefaultSpringPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtil;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.commons.gradle.GradleCore;
import org.springframework.ide.vscode.commons.gradle.GradleProjectManager;
import org.springframework.ide.vscode.commons.languageserver.LaunguageServerApp;
import org.springframework.ide.vscode.commons.languageserver.java.CompositeJavaProjectManager;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectManager;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.java.MavenProjectManager;
import org.springframework.ide.vscode.commons.maven.java.classpathfile.JavaProjectWithClasspathFileManager;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * Starts up Language Server process
 * 
 * @author Alex Boyko
 * @author Kris De Volder
 *
 */
public class Main {
		
	public static void main(String[] args) throws IOException, InterruptedException {
		LaunguageServerApp.start(() -> {
			CompositeJavaProjectManager javaProjectManager = new CompositeJavaProjectManager(new JavaProjectManager[] {
					new MavenProjectManager(MavenCore.getDefault()),
					new GradleProjectManager(GradleCore.getDefault()),
					new JavaProjectWithClasspathFileManager()
			});
			DefaultSpringPropertyIndexProvider indexProvider = new DefaultSpringPropertyIndexProvider(javaProjectManager);
			TypeUtilProvider typeUtilProvider = (IDocument doc) -> new TypeUtil(javaProjectManager.find(doc));
			SimpleLanguageServer server = new BootPropertiesLanguageServer(indexProvider, typeUtilProvider, javaProjectManager);
			javaProjectManager.setFileObserver(server.getWorkspaceService());
			indexProvider.setProgressService(server.getProgressService());
			return server;
		});
	}

}
