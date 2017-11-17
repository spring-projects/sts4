/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.bosh;

import java.io.IOException;

import org.springframework.ide.vscode.bosh.models.BoshCommandCloudConfigProvider;
import org.springframework.ide.vscode.bosh.models.BoshCommandReleasesProvider;
import org.springframework.ide.vscode.bosh.models.BoshCommandStemcellsProvider;
import org.springframework.ide.vscode.commons.languageserver.LaunguageServerApp;
import org.springframework.ide.vscode.commons.util.Log;

public class Main {
	public static void main(String[] args) throws IOException, InterruptedException {
		String serverName = "bosh-language-server";
		Log.redirectToFile(serverName);
		BoshCliConfig cliConfig = new BoshCliConfig();
		LaunguageServerApp.start(serverName, () -> new BoshLanguageServer(
				cliConfig,
				new BoshCommandCloudConfigProvider(cliConfig),
				new BoshCommandStemcellsProvider(cliConfig),
				new BoshCommandReleasesProvider(cliConfig)
		));
	}
}
