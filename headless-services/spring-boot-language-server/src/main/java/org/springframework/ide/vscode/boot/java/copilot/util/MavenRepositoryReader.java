package org.springframework.ide.vscode.boot.java.copilot.util;

public class MavenRepositoryReader extends AbstractMavenReader {

	public MavenRepositoryReader() {
		this.sectionName = "repository";
	}

	protected String massageText(String text) {
		if (text.contains("<repositories>")) {
			return text;
		}
		else {
			return "<repositories>" + text + "</repositories>";
		}
	}

}
