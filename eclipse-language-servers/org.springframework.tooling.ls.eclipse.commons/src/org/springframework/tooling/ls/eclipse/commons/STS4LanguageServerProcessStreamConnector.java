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
import java.util.List;

import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

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
	
	protected String getWorkingDirLocation() {
		return System.getProperty("user.dir");
	}
	
}
