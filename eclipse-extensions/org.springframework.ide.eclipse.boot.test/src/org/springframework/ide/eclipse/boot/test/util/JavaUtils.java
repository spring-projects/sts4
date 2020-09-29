/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.test.util;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ExternalCommand;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ExternalProcess;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * Wrapper around a Eclipse IVMInstall to make it easier to launch
 * 'plain' JVM processes in testing code. I.e. not using the Eclipse
 * debug framework but using simple JRE ProcessBuilder api.
 *
 * @author Kris De Volder
 */
public class JavaUtils {

	private IVMInstall jvm;

	public JavaUtils(IVMInstall jvm) {
		this.jvm = jvm;
	}

	public JavaUtils() {
		this(JavaRuntime.getDefaultVMInstall());
	}

	/**
	 * Run an executable jar as an external Java process.
	 */
	public LaunchResult runJar(File jarFile) throws Exception {
		File java = getJavaExecutable();
		Assert.isNotNull(java, "Couldn't find a 'java' executable");
		File workdir = StsTestUtil.createTempDirectory("javalaunch-work", "dir");
		ExternalCommand cmd = new ExternalCommand(java.toString(), "-jar", jarFile.toString());
		ExternalProcess process = new ExternalProcess(workdir, cmd, true);
		return new LaunchResult(process.getExitValue(), process.getOut(),process.getErr());
	}

	public File getJavaExecutable() {
		File javaHome = getJavaHome();
		for (String exePath : getJavaExecutableLocations()) {
			File exe = new File(javaHome, exePath);
			if (exe.isFile()) {
				return exe;
			}
		}
		return null;
	}

	/**
	 * Paths relative to Java home where we will look for the 'java' executable
	 */
	protected String[] getJavaExecutableLocations() {
		return new String[] {
				"bin/java",
				"bin/java.exe"
		};
	}

	public File getJavaHome() {
		return jvm.getInstallLocation();
	}
}
