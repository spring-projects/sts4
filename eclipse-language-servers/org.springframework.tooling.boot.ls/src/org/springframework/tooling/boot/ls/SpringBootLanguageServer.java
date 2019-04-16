/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls;

import static org.springframework.tooling.ls.eclipse.commons.preferences.LanguageServerConsolePreferenceConstants.SPRING_BOOT_SERVER;

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
import org.springframework.tooling.ls.eclipse.commons.JRE;
import org.springframework.tooling.ls.eclipse.commons.JRE.MissingJDKException;
import org.springframework.tooling.ls.eclipse.commons.STS4LanguageServerProcessStreamConnector;

/**
 * @author Martin Lippert
 */
public class SpringBootLanguageServer extends STS4LanguageServerProcessStreamConnector {
	
	public SpringBootLanguageServer() {
		super(SPRING_BOOT_SERVER);
		setCommands(getJRE().jarLaunchCommand(getLanguageServerJARLocation(), 
				getJVMArgs()));
		setWorkingDirectory(getWorkingDirLocation());
	}
	
	private List<String> getJVMArgs() {
		List<String> args = new ArrayList<>();
		
//		args.add("-Xdebug");
//		args.add("-Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n");
		args.add("-Dsts.lsp.client=eclipse");
		args.add("-Dlsp.completions.indentation.enable=true");
		args.add("-Xmx1024m");
		
		addCustomJVMArgs(args);
		
		return args;
	}

	private void addCustomJVMArgs(List<String> args) {
		String customArgs = System.getProperty("boot.ls.custom.vmargs");
		
		if (customArgs != null) {
			String prefix = "";
			String[] separateArgs = customArgs.split(",-");
			for (String arg : separateArgs) {
				args.add(prefix + arg);
				prefix = "-";
			}
		}
	}

	private JRE getJRE() {
		try {
			return JRE.findJRE(true);
		} catch (MissingJDKException e) {
			MissingJdkWarning.show(e);
			return new JRE(e.javaHome, null); //Not everything will work without tools jar. But some of it will. So fallback on JRE without toolsjar.
		}
	}

	protected String getLanguageServerJARLocation() {
		String languageServer = "spring-boot-language-server-" + Constants.LANGUAGE_SERVER_VERSION;

		Bundle bundle = Platform.getBundle(Constants.PLUGIN_ID);
		String bundleVersion = bundle.getVersion().toString();

		String languageServerLocalCopy = bundleVersion + "-" + languageServer;
		
		File dataFile = bundle.getDataFile(languageServerLocalCopy);

		Exception error = null;
		if (!dataFile.exists() || bundleVersion.endsWith("qualifier")) { // qualifier check to get the language server always copied in dev mode
			try {
				copyLanguageServerJAR(languageServer, languageServerLocalCopy);
			}
			catch (Exception e) {
				error = e;
			}
		}
		
		if (bundleVersion.endsWith("qualifier")) {
			File userHome = new File(System.getProperty("user.home"));
			File locallyBuiltJar = new File(
					userHome, 
					"git/sts4/headless-services/spring-boot-language-server/target/spring-boot-language-server-" + Constants.LANGUAGE_SERVER_VERSION
			);
			if (locallyBuiltJar.exists()) {
				return locallyBuiltJar.getAbsolutePath();
			}
			if (error!=null) {
				error.printStackTrace();
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
