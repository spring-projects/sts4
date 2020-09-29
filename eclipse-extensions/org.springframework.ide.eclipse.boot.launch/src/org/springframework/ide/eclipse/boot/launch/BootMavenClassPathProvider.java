/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.m2e.jdt.internal.launch.MavenRuntimeClasspathProvider;

@SuppressWarnings("restriction")
public class BootMavenClassPathProvider extends MavenRuntimeClasspathProvider {

	@Override
	public IRuntimeClasspathEntry[] resolveClasspath(IRuntimeClasspathEntry[] entries,
			ILaunchConfiguration configuration) throws CoreException {
		return super.resolveClasspath(entries, BootLaunchConfigurationDelegate.copyAs(configuration, JDT_JAVA_APPLICATION));
	}

}
