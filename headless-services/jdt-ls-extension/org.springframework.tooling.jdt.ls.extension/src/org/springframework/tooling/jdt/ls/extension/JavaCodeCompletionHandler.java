/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.extension;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.springframework.ide.vscode.commons.protocol.java.JavaCodeCompleteParams;

import com.google.gson.Gson;

@SuppressWarnings("restriction")
public class JavaCodeCompletionHandler implements IDelegateCommandHandler {
	
	private Gson gson = new Gson();

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		JavaCodeCompleteParams params = gson.fromJson(gson.toJson(arguments.get(0)), JavaCodeCompleteParams.class);
		return JavaHelpers.CODE_COMPLETIONS.get().codeComplete(params.getProjectUri(), params.getPrefix(), params.isIncludeTypes(), params.isIncludePackages());
	}

}
