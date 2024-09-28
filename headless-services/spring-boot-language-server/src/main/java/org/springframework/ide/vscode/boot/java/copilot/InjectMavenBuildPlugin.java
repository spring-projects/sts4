package org.springframework.ide.vscode.boot.java.copilot;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Mark Pollack
 */
public class InjectMavenBuildPlugin {

	private String text;

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public InjectMavenBuildPlugin(@JsonProperty("text") String text) {
		this.text = Objects.requireNonNull(text);
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return "InjectMavenBuildPlugin{" + "text='" + text + '\'' + '}';
	}

}