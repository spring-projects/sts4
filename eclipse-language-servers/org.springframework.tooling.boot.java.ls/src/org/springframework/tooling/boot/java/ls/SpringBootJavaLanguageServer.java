/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.java.ls;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.springframework.tooling.ls.eclipse.commons.STS4LanguageServerProcessStreamConnector;

/**
 * @author Martin Lippert
 */
public class SpringBootJavaLanguageServer extends STS4LanguageServerProcessStreamConnector {
	
	public SpringBootJavaLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(getJDKLocation());
		
//		commands.add("-Xdebug");
//		commands.add("-Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n");
		
		commands.add("-Dlsp.lazy.completions.disable=true");
		commands.add("-Dlsp.completions.indentation.enable=true");
		commands.add("-Xmx1024m");
		commands.add("-cp");
		commands.add(getToolsJAR() + File.pathSeparator + getLanguageServerJARLocation());
		commands.add("org.springframework.boot.loader.JarLauncher");

		String workingDir = getWorkingDirLocation();

		setCommands(commands);
		setWorkingDirectory(workingDir);
	}
	
	protected String getLanguageServerJARLocation() {
		String fromSysprop = System.getProperty("boot-java-ls-jar", null);
		if (fromSysprop!=null) {
			return fromSysprop;
		}
		String languageServer = "boot-java-language-server-" + Constants.LANGUAGE_SERVER_VERSION;

		Bundle bundle = Platform.getBundle(Constants.PLUGIN_ID);
		String bundleVersion = bundle.getVersion().toString();

		String languageServerLocalCopy = bundleVersion + "-" + languageServer;
		
		File dataFile = bundle.getDataFile(languageServerLocalCopy);
		if (!dataFile.exists() || bundleVersion.endsWith("qualifier")) { // qualifier check to get the language server always copied in dev mode
			try {
				copyLanguageServerJAR(languageServer, languageServerLocalCopy);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return dataFile.getAbsolutePath();
	}
	
	protected void copyLanguageServerJAR(String languageServerJarName, String languageServerLocalCopy) throws Exception {
		Bundle bundle = Platform.getBundle(Constants.PLUGIN_ID);
		InputStream stream = FileLocator.openStream( bundle, new Path("servers/" + languageServerJarName), false );
		
		File dataFile = bundle.getDataFile(languageServerLocalCopy);
		Files.copy(stream, dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

}
