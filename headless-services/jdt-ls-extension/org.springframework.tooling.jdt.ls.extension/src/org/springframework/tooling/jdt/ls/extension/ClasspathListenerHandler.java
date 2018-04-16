/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.extension;

import static org.springframework.tooling.jdt.ls.commons.Logger.log;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.JavaClientConnection;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.classpath.ClientCommandExecutor;
import org.springframework.tooling.jdt.ls.commons.classpath.ReusableClasspathListenerHandler;

@SuppressWarnings("restriction")
public class ClasspathListenerHandler implements IDelegateCommandHandler {
	
	private static ReusableClasspathListenerHandler handlerImpl = checkSupported();
	private static ReusableClasspathListenerHandler checkSupported() {
		try {
			JavaClientConnection.class.getMethod("executeClientCommand", String.class, Object[].class);
			return new ReusableClasspathListenerHandler(new ClientCommandExecutor() {
				@Override
				public Object executeClientCommand(String id, Object... params) {
					return JavaLanguageServerPlugin.getInstance().getClientConnection().executeClientCommand(id, params);
				}
			});
		} catch (Exception e) {
			Logger.log(e);
		}
		return null;
	}

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		if (handlerImpl==null) {
			throw new UnsupportedOperationException("Command '"+commandId+"' not supported on older versions of JDT Language Server");
		}
		log("ClasspathListenerHandler executeCommand " + commandId + ", " + arguments);
		switch (commandId) {
		case "sts.java.addClasspathListener":
			return addClasspathListener((String) arguments.get(0));
		case "sts.java.removeClasspathListener":
			return removeClasspathListener((String) arguments.get(0));
		default:
			throw new IllegalArgumentException("Unknown command id: " + commandId);
		}
	}

	private Object removeClasspathListener(String callbackCommandId) {
		log("ClasspathListenerHandler removeClasspathListener " + callbackCommandId);
		return handlerImpl.removeClasspathListener(callbackCommandId);
	}

	private Object addClasspathListener(String callbackCommandId) {
		log("ClasspathListenerHandler addClasspathListener " + callbackCommandId);
		handlerImpl.addClasspathListener(callbackCommandId);
		log("ClasspathListenerHandler addClasspathListener " + callbackCommandId + " => OK");
		return "ok";
	}

}
