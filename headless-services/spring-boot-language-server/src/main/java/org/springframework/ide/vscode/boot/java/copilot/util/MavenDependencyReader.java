package org.springframework.ide.vscode.boot.java.copilot.util;

public class MavenDependencyReader extends AbstractMavenReader {

	public MavenDependencyReader() {
		this.sectionName = "dependency";
	}

	protected String massageText(String text) {
		if (text.contains("<dependencies>")) {
			return text;
		}
		else {
			return "<dependencies>" + text + "</dependencies>";
		}
	}

}