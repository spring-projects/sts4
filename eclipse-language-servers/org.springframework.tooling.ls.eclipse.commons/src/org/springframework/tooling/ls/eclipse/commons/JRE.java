/*******************************************************************************
 * Copyright (c) 2018, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jdt.internal.launching.StandardVMType;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("restriction")
public class JRE {

	public final File javaHome;
	public final File toolsJar;

	public JRE(File javaHome, File toolsJar) {
		this.javaHome = javaHome;
		this.toolsJar = toolsJar;
	}

	public String getJavaExecutable() {
		if (javaHome.exists()) {
			File javaExecutable = StandardVMType.findJavaExecutable(javaHome);
			if (javaExecutable != null && javaExecutable.isFile()) {
				return javaExecutable.getAbsolutePath();
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "JRE("+javaHome+")";
	}

	public static JRE currentJRE() {
		return new JRE(new File(System.getProperty("java.home")), null);
	}

	/**
	 * Get a JRE, with a paired tools jar if it is needed based on current JRE version
	 * and whether the caller wants it.
	 *
	 * @return The JRE.
	 * @throws MissingToolsJarException If tools jar is needed but could not be found.
	 * @throws MissingJDKException
	 */
	public static JRE findJRE(boolean needJdk) throws MissingJDKException, MissingToolsJarException {
		File mainHome = new File(System.getProperty("java.home"));

		if (!needJdk) {
			return new JRE(mainHome, null);
		}
		else if (!javaVersionNeedsToolsJar()) {
			return new JRE(mainHome, null);
		}
		else {
			Set<File> jhomes = new LinkedHashSet<>();
			jhomes.add(mainHome);

			findPairedJdk(mainHome, jhomes::add);

			//Finding a JDK with a tools jar
			List<File> lookedIn = new ArrayList<>();
			for (File jhome : jhomes) {
				for (String tjPath : TOOLS_JAR_PATHS) {
					File toolsJar = new File(jhome, tjPath).toPath().normalize().toFile();
					lookedIn.add(toolsJar);
					if (toolsJar.isFile()) {
						return new JRE(jhome, toolsJar);
					}
				}
			}
			throw new MissingToolsJarException(mainHome, lookedIn);
		}
	}

	/**
	 * Different places to look for tools jar, relative to Java home (for java version < 9)
	 */
	private final static String[] TOOLS_JAR_PATHS = {
			"../lib/tools.jar",
			"lib/tools.jar"
	};

	private static boolean javaVersionNeedsToolsJar() {
		String versionString = System.getProperty("java.version");
		int javaVersion = parseVersion(versionString);
		return javaVersion<9;
	}

	public static int parseVersion(String versionString) {
		int dash = versionString.indexOf('-');
		if (dash>=0) {
			versionString = versionString.substring(0, dash);
		}
		int javaVersion = Integer.parseInt(versionString.split("\\.")[0]);
		return javaVersion;
	}

	private static void findPairedJdk(File mainHome, Consumer<File> requestor) {
		//Mainly for windows where it is common to have side-by-side install of a jre and jdk, instead of a
		//nested jre install inside of a jdk.

		//E.g.
		//C:\ProgramFiles\Java\jdk1.8.0_161
		//C:\ProgramFiles\Java\jre1.8.0_161

		String name = mainHome.getName();
		String pairedName = name.replace("jre", "jdk");
		if (!pairedName.equals(name)) {
			File pairedJdk = new File(mainHome.getParentFile(), pairedName);
			if (pairedJdk.exists()) {
				requestor.accept(pairedJdk);
			}
		}
	}

	@SuppressWarnings("serial")
	public static class MissingToolsJarException extends MissingJDKException {

		public final List<File> lookedIn;

		public MissingToolsJarException(File javaHome, List<File> lookedIn) {
			super(javaHome);
			this.lookedIn = lookedIn;
		}
	}

	@SuppressWarnings("serial")
	public static class MissingJDKException extends Exception {
		public MissingJDKException(File javaHome) {
			super();
			this.javaHome = javaHome;
		}
		public final File javaHome;
	}

	/**
	 * Creates a 'command' that can be used to launch a executable jar
	 * with this jre. The tools.jar, if available, is added automatically to the classpath
	 * via a "-Dloader.path" argument. For this to work properly, the executable
	 * jar should be packaged with Spring Boot Properties loader (i.e. specify `ZIP` layout
	 * in the spring-boot-maven plugin configuration)
	 */
	public List<String> jarLaunchCommand(String jarLocation, List<String> vmargs) {
		ImmutableList.Builder<String> command = ImmutableList.builder();
		command.add(getJavaExecutable());
		if (toolsJar!=null) {
			command.add("-Dloader.path="+toolsJar);
		}
		command.addAll(vmargs);
		command.add("-jar");
		command.add(jarLocation);
		return command.build();
	}
}
