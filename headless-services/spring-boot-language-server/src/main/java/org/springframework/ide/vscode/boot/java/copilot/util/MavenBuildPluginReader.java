package org.springframework.ide.vscode.boot.java.copilot.util;

public class MavenBuildPluginReader extends AbstractMavenReader {

	public MavenBuildPluginReader() {
		this.sectionName = "plugin";
	}

	protected String massageText(String text) {
		if (text.contains("<plugins>")) {
			return text;
		}
		else {
			return "<plugins>" + text + "</plugins>";
		}
	}

}