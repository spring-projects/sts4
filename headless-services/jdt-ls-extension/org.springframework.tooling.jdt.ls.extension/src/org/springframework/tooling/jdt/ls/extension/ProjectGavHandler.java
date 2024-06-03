/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.extension;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.springframework.ide.vscode.commons.protocol.java.ProjectGavParams;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.java.BuildInfo;

import com.google.gson.Gson;

public class ProjectGavHandler implements IDelegateCommandHandler {
	
	private Gson gson = new Gson();

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		JdtLsExtensionPlugin.getInstance().getLog().error("Recieved uris: " + arguments.get(0));
		try {
			ProjectGavParams params = gson.fromJson(gson.toJson(arguments.get(0)), ProjectGavParams.class);
			JdtLsExtensionPlugin.getInstance().getLog().error("Params project number: " + params.projectUris().size());
			return BuildInfo.projectGAV(params, executor, Logger.forEclipsePlugin(() -> JdtLsExtensionPlugin.getInstance())).get();
		} finally {
			executor.shutdown();
		}
	}

}
