/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
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
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.springframework.ide.vscode.commons.protocol.java.JavaDataParams;

@SuppressWarnings("restriction")
public class JavaTypeHandler implements IDelegateCommandHandler {
	
	@SuppressWarnings("unchecked")
	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		Map<String, Object> obj = (Map<String, Object>) arguments.get(0);
		String uri = (String) obj.get("projectUri");
		String bindingKey = (String) obj.get("bindingKey");
		Boolean lookInOtherProjects = (Boolean) obj.get("lookInOtherProjects");
		return JavaHelpers.DATA.get().typeData(uri, bindingKey, JavaDataParams.isLookInOtherProjects(uri, lookInOtherProjects));
	}


}
