/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
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

import org.springframework.ide.vscode.commons.languageserver.LaunguageServerApp;
import org.springframework.ide.vscode.commons.util.LogRedirect;

/**
 * Starts up Language Server process
 * 
 * @author Alex Boyko
 * @author Kris De Volder
 *
 */
public class Main {
		
	public static void main(String[] args) throws IOException, InterruptedException {
		String serverName = "boot-language-server";
		LogRedirect.redirectToFile(serverName);
		//TODO: wrap both BootProperties and BootJavaLanguageServers into a composite of some kind.
		LaunguageServerApp.start(serverName,
				() -> BootLanguageServer.create(BootLanguageServerParams.createDefault()).getServer()
		);
	}

}
