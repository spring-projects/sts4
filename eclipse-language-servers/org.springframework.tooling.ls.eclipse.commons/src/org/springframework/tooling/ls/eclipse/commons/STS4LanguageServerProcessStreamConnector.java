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
package org.springframework.tooling.ls.eclipse.commons;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

@SuppressWarnings("restriction")
public class STS4LanguageServerProcessStreamConnector extends ProcessStreamConnectionProvider {

	private static LanguageServerProcessReaper processReaper = new LanguageServerProcessReaper();
	
	@Override
	public void start() throws IOException {
		super.start();
		processReaper.addProcess(LanguageServerProcessReaper.getProcess(this));
	}
	
	@Override
	public void stop() {
		super.stop();
		processReaper.removeProcess(LanguageServerProcessReaper.getProcess(this));
	}
	
	protected String getJDKLocation() {
		try {
			File javaHome= new File(System.getProperty("java.home")).getCanonicalFile(); //$NON-NLS-1$
			if (javaHome.exists()) {
				File javaExecutable = StandardVMType.findJavaExecutable(javaHome);
				if (javaExecutable != null && javaExecutable.exists()) {
					return javaExecutable.getAbsolutePath();
				}
			}
		} catch (IOException e) {
			return null;
		}
		
		return null;
	}
	
	/**
	 * Different places to look for tools jar, relative to Java home.
	 */
	private static String [] TOOLS_JAR_PATHS = {
			"../lib/tools.jar",
			"lib/tools.jar"
	};
	
	protected File getToolsJAR() {
		List<File> jhomes = new ArrayList<>(2);
		resolve(System.getenv("JAVA_HOME"), jhomes::add);
		resolve(System.getProperty("java.home"), jhomes::add);
		for (File jhome : jhomes) {
			for (String tjPath : TOOLS_JAR_PATHS) {
				File toolsJar = new File(jhome, tjPath);
				if (toolsJar.isFile()) {
					return toolsJar;
				}
			}
		}
		return null;
	}
	
	private void resolve(String path, Consumer<File> requestor) {
		if (path!=null) {
			try {
				File file = new File(path).getCanonicalFile();
				if (file.exists()) {
					requestor.accept(file);
					return;
				}
			} catch (IOException e) {
				//Ignore... this can happen because it tries to handle symbolic links
			}
		}
	}
	
	private File getJavaHomeFromEnv() {
		try {
			File jhome = new File(System.getenv("JAVA_HOME"));
			if (jhome!=null) {
				jhome = jhome.getCanonicalFile();
				if (jhome.isDirectory()) {
					return jhome;
				}
			}
		} catch (IOException e) {
			//Ignore... this can happen because it tries to handle symbolic links
		}
		return null;
	}

	protected String getWorkingDirLocation() {
		return System.getProperty("user.dir");
	}
	
}
