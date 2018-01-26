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

import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

@SuppressWarnings("restriction")
public class STS4LanguageServerProcessStreamConnector extends ProcessStreamConnectionProvider {

	public static class MissingToolsJarException extends Exception {
		
		public final File javaHome;
		public final List<File> lookedIn;
		
		public MissingToolsJarException(File javaHome, List<File> lookedIn) {
			super();
			this.javaHome = javaHome;
			this.lookedIn = lookedIn;
		}
	}

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
	private static String[] TOOLS_JAR_PATHS = {
			"../lib/tools.jar",
			"lib/tools.jar"
	};
	
	/**
	 * Get a tools jar if it is needed based on current JRE version.
	 * 
	 * @return The tools.jar, or null if none is needed.
	 * @throws MissingToolsJarException If tools jar is needed but could not be found.
	 */
	protected File getToolsJAR() throws MissingToolsJarException {
		List<File> lookedIn = new ArrayList<>();
		File jhome = new File(System.getProperty("java.home"));
//		resolve(System.getenv("JAVA_HOME"), jhomes::add);
		for (String tjPath : TOOLS_JAR_PATHS) {
			File toolsJar = new File(jhome, tjPath).toPath().normalize().toFile();
			lookedIn.add(toolsJar);
			if (toolsJar.isFile()) {
				return toolsJar;
			}
		}
		throw new MissingToolsJarException(jhome, lookedIn);
	}
	
	protected String getWorkingDirLocation() {
		return System.getProperty("user.dir");
	}
	
}
