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
package org.springframework.tooling.ls.eclipse.commons.commands;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.lsp4e.command.LSPCommandHandler;
import org.eclipse.lsp4j.Command;
import org.springframework.tooling.ls.eclipse.commons.STS4LanguageClientImpl;

public class JavaWorkspaceCommandHanlder extends LSPCommandHandler {

	public static final String JAVA_WORKSPACE_COMMAND = "java.execute.workspaceCommand";

	public static final String ADD_CLASSPATH_LISTENER_COMMAND = "sts.java.addClasspathListener";
	public static final String REMOVE_CLASSPATH_LISTENER_COMMAND = "sts.java.removeClasspathListener";

	@Override
	public Object execute(ExecutionEvent event, Command command, IPath path) throws ExecutionException {
		if (JAVA_WORKSPACE_COMMAND.equals(command.getCommand())) {
			String cmdId = (String) command.getArguments().get(0);
			List<Object> arguments = command.getArguments().subList(1, command.getArguments().size());

			switch (cmdId) {
			case ADD_CLASSPATH_LISTENER_COMMAND:
				String callbackCommandIdForAdd = (String) arguments.get(0);
				Boolean batched = (Boolean) arguments.get(1);
				return STS4LanguageClientImpl.CLASSPATH_SERVICE.addClasspathListener(callbackCommandIdForAdd, batched);
			case REMOVE_CLASSPATH_LISTENER_COMMAND:
				String callbackCommandIdForRemove = (String) arguments.get(0);
				return STS4LanguageClientImpl.CLASSPATH_SERVICE.removeClasspathListener(callbackCommandIdForRemove);
			}
		}
		return null;
	}

}
