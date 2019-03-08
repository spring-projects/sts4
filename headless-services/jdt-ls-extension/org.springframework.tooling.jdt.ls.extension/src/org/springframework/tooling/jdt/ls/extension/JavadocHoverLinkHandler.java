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

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.javadoc.JavaElementLinks;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.java.JavaData;

public class JavadocHoverLinkHandler implements IDelegateCommandHandler {
	
	private static final Logger logger = Logger.DEFAULT;

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		Map<String, Object> obj = (Map<String, Object>) arguments.get(0);
		String uri = (String) obj.get("projectUri");
		String bindingKey = (String) obj.get("bindingKey");
		Boolean lookInOtherProjectsObj = (Boolean) obj.get("lookInOtherProjects");
		boolean lookInOtherProjects = uri == null ? true : lookInOtherProjectsObj == null ? false : lookInOtherProjectsObj.booleanValue();
		try {
			IJavaElement element = JavaData.findElement(uri == null ? null : URI.create(uri), bindingKey, lookInOtherProjects);
			if (element != null) {
				// Bug in JDT server one '(' not encoded while everything else in the query is
				// encoded
				return JavaElementLinks.createURI(null, element).replace("(", "%28");
			}
		} catch (Exception e) {
			logger.log(e);
		}
		return null;
	}

}
