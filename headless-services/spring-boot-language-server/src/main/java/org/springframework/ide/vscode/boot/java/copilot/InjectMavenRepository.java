package org.springframework.ide.vscode.boot.java.copilot;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Mark Pollack
 */
public class InjectMavenRepository {

	private String text;

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public InjectMavenRepository(@JsonProperty("text") String text) {
		this.text = Objects.requireNonNull(text);
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return "InjectMavenRepository{" + "text='" + text + '\'' + '}';
	}

}
