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

import java.util.ArrayList;
import java.util.List;

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

	@Override
	protected String getLanguageServerArtifactId() {
		return "spring-boot-language-server";
	}

	@Override
	protected String getPluginId() {
		return Constants.PLUGIN_ID;
	}
}
