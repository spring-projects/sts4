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