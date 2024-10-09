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
package org.springframework.ide.vscode.boot.java.copilot;

import java.util.ArrayList;
import java.util.List;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.FencedCodeBlock;

public class MarkdownResponseVisitor extends AbstractVisitor {

	private List<ProjectArtifact> projectArtifacts = new ArrayList<>();

	@Override
	public void visit(FencedCodeBlock fencedCodeBlock) {
		String info = fencedCodeBlock.getInfo();
		String code = fencedCodeBlock.getLiteral();
		if (info.equalsIgnoreCase("java")) {
			addJavaCode(code);
		}
		if (info.equalsIgnoreCase("xml")) {
			addMavenDependencies(code);
		}
		if (info.equalsIgnoreCase("properties")) {
			addApplicationProperties(code);
		}
		if (info.equalsIgnoreCase("html")) {
			addHtml(code);
		}
		if (info.isBlank()) {
			// sometimes the response doesn't contain
			if (code.contains("package")) {
				addJavaCode(code);
			}
			else if (code.contains("<dependency>")) {
				addMavenDependencies(code);
			}
		}

	}

	private void addHtml(String code) {
		addIfNotPresent(new ProjectArtifact(ProjectArtifactType.HTML, code));
	}

	private void addApplicationProperties(String code) {
		addIfNotPresent(new ProjectArtifact(ProjectArtifactType.APPLICATION_PROPERTIES, code));
	}

	private void addMavenDependencies(String code) {
		addIfNotPresent(new ProjectArtifact(ProjectArtifactType.MAVEN_DEPENDENCIES, code));
	}

	private void addJavaCode(String code) {
		if (code.contains("@SpringBootApplication") && code.contains("SpringApplication.run")) {
			addIfNotPresent(new ProjectArtifact(ProjectArtifactType.MAIN_CLASS, code));
		}
		else if (code.contains("@Test")) {
			addIfNotPresent(new ProjectArtifact(ProjectArtifactType.TEST_CODE, code));
		}
		else {
			addIfNotPresent(new ProjectArtifact(ProjectArtifactType.SOURCE_CODE, code));
		}
	}

	private void addIfNotPresent(ProjectArtifact projectArtifact) {
		// TODO - investigate why Node has duplicate entries
		if (!this.projectArtifacts.contains(projectArtifact)) {
			this.projectArtifacts.add(projectArtifact);
		}
		else {
			// System.out.println("duplicate project artifact :(");
		}
	}

	public List<ProjectArtifact> getProjectArtifacts() {
		return this.projectArtifacts;
	}

}
