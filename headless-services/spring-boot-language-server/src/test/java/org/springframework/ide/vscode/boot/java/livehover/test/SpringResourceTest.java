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
package org.springframework.ide.vscode.boot.java.livehover.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.ide.vscode.boot.java.links.EclipseSourceLinks;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.utils.SpringResource;
import org.springframework.ide.vscode.boot.java.value.test.MockProjects;
import org.springframework.ide.vscode.boot.java.value.test.MockProjects.MockProject;

public class SpringResourceTest {

	private MockProjects projects = new MockProjects();
	private MockProject project = projects.create("test-project");

	private SourceLinks sourceLinks = new EclipseSourceLinks();

	@Test public void vcapResourceToMarkdown() throws Exception {
		assertEquals(
				"[com/github/kdvolder/helloworldservice/Greeter.class]" +
				"(http://org.eclipse.ui.intro/execute?command=org.springframework.tooling.boot.ls.OpenJavaType%28fqName%3Dcom.github.kdvolder.helloworldservice.Greeter%2CprojectName%3Dtest-project%29)"
			,
				toMarkdown("file [/home/vcap/app/com/github/kdvolder/helloworldservice/Greeter.class]")
		);
	}

	private String toMarkdown(String beanResourceString) {
		return new SpringResource(sourceLinks, beanResourceString, project).toMarkdown();
	}

}
