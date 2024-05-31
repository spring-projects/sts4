/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.boot.app.ai.VectorStoreHandler;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.commands.WorkspaceBootExecutableProjects;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

@Configuration(proxyBeanMethods = false)
public class CommandsConfig {
	
	@Bean WorkspaceBootExecutableProjects workspaceBootProjects(SimpleLanguageServer server, JavaProjectFinder projectFinder, SpringSymbolIndex symbolIndex, SpringMetamodelIndex springMetamodelIndex) {
		return new WorkspaceBootExecutableProjects(server, projectFinder, symbolIndex, springMetamodelIndex);
	}
	
	@Bean VectorStoreHandler vectorStoreHandler(SimpleLanguageServer server, VectorStore vectorStore, ChatClient aiClient) {
	    return new VectorStoreHandler(server, vectorStore, aiClient);
    }

}
