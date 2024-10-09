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

import java.util.List;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

public class ProjectArtifactCreator {

	public List<ProjectArtifact> create(String response) {
		Parser parser = Parser.builder().build();
		Node node = parser.parse(response);

		MarkdownResponseVisitor markdownResponseVisitor = new MarkdownResponseVisitor();
		node.accept(markdownResponseVisitor);

		return markdownResponseVisitor.getProjectArtifacts();
	}

}