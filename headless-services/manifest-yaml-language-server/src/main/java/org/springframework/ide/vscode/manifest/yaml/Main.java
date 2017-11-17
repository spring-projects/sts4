/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.manifest.yaml;

import java.io.IOException;

import org.springframework.ide.vscode.commons.languageserver.LaunguageServerApp;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.Log;

public class Main {
	SimpleLanguageServer server = new ManifestYamlLanguageServer();

	public static void main(String[] args) throws IOException, InterruptedException {
		String serverName = "manifest-yaml-language-server";
		Log.redirectToFile(serverName);
		LaunguageServerApp.start(serverName, ManifestYamlLanguageServer::new);
	}

}
