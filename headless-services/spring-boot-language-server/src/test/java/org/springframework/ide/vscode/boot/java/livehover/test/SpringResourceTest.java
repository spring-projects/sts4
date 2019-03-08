/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.ide.vscode.boot.java.links.SourceLinkFactory;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.utils.SpringResource;
import org.springframework.ide.vscode.boot.java.value.test.MockProjects;
import org.springframework.ide.vscode.boot.java.value.test.MockProjects.MockProject;

public class SpringResourceTest {

	private MockProjects projects = new MockProjects();
	private MockProject project = projects.create("test-project");

	private SourceLinks sourceLinks = SourceLinkFactory.NO_SOURCE_LINKS;

	@Test public void vcapResourceToMarkdown() throws Exception {
		assertEquals(
				"`com/github/kdvolder/helloworldservice/Greeter.class`",
				toMarkdown("file [/home/vcap/app/com/github/kdvolder/helloworldservice/Greeter.class]")
		);
	}

	private String toMarkdown(String beanResourceString) {
		return new SpringResource(sourceLinks, beanResourceString, project).toMarkdown();
	}

}
