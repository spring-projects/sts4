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

import java.io.File;
import java.io.IOException;

import org.springframework.ide.vscode.commons.languageserver.LaunguageServerApp;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

public class Main {
   	SimpleLanguageServer server = new ManifestYamlLanguageServer();

   	public static void main(String[] args) throws IOException, InterruptedException {
   		File logfile = null;
   		if (System.getProperty("org.slf4j.simpleLogger.logFile") == null) {
   			logfile = File.createTempFile("manifest-yaml-language-server", ".log");
   			System.setProperty("org.slf4j.simpleLogger.logFile", logfile.toString());
   		} else {
   			logfile = new File(System.getProperty("org.slf4j.simpleLogger.logFile"));
   		}
		System.err.println("Redirecting log output to: "+logfile);
		LaunguageServerApp.start(ManifestYamlLanguageServer::new);
	}

}
