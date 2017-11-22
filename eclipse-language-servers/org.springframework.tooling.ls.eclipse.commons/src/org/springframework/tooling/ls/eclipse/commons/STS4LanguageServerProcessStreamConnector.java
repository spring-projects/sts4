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

import org.eclipse.core.runtime.Path;
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
	
	protected String getToolsJAR() {
		File jre = new File(System.getProperty("java.home"));
		return new File(jre.getParent(), "lib" + Path.SEPARATOR + "tools.jar").getAbsolutePath();
	}
	
	protected String getWorkingDirLocation() {
		return System.getProperty("user.dir");
	}
	
}
