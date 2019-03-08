/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.bosh.ls;

import static org.springframework.tooling.ls.eclipse.commons.preferences.LanguageServerConsolePreferenceConstants.BOSH_SERVER;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.springframework.tooling.ls.eclipse.commons.JRE;
import org.springframework.tooling.ls.eclipse.commons.STS4LanguageServerProcessStreamConnector;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class BoshLanguageServer extends STS4LanguageServerProcessStreamConnector {

	public BoshLanguageServer() {
		super(BOSH_SERVER);
		setCommands(JRE.currentJRE().jarLaunchCommand(getLanguageServerJARLocation(), ImmutableList.of(
				//"-Xdebug",
				//"-agentlib:jdwp=transport=dt_socket,address=8899,server=y,suspend=n",
				"-Dlsp.lazy.completions.disable=true",
				"-Dlsp.completions.indentation.enable=true"
		)));
		setWorkingDirectory(getWorkingDirLocation());
	}
	
	protected String getLanguageServerJARLocation() {
		String languageServer = "bosh-language-server-" + Constants.LANGUAGE_SERVER_VERSION + ".jar";

		Bundle bundle = Platform.getBundle(Constants.PLUGIN_ID);
		File dataFile = bundle.getDataFile(languageServer);
//		if (!dataFile.exists()) {
			try {
				copyLanguageServerJAR(languageServer);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
//		}
		
		return dataFile.getAbsolutePath();
	}
	
	protected void copyLanguageServerJAR(String languageServerJarName) throws Exception {
		Bundle bundle = Platform.getBundle(Constants.PLUGIN_ID);
		InputStream stream = FileLocator.openStream( bundle, new Path("servers/" + languageServerJarName), false );
		
		File dataFile = bundle.getDataFile(languageServerJarName);
		Files.copy(stream, dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

}
