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
package org.springframework.tooling.jdt.ls.commons.java;

import java.net.URI;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.vscode.commons.protocol.java.JavaCodeCompleteData;
import org.springframework.tooling.jdt.ls.commons.resources.ResourceUtils;

/**
 * @author Martin Lippert
 */
public class JavaCodeCompletion {

	public List<JavaCodeCompleteData> codeComplete(String project, String prefix, boolean includeTypes, boolean includePackages) throws Exception {
		URI projectUri = project == null ? null : URI.create(project);
		IJavaProject javaProject = projectUri == null ? null : ResourceUtils.getJavaProject(projectUri);
		
		JavaCodeCompletionProposalCollector collector = new JavaCodeCompletionProposalCollector(includeTypes, includePackages);
		JavaCodeCompletionUtils.codeComplete(javaProject, prefix, collector);
		
		return collector.getProposals();
	}

}
