package org.springframework.ide.vscode.boot.java.copilot;

import java.util.Objects;

public class ProjectArtifact {

	private ProjectArtifactType artifactType;

	private String text;

	public ProjectArtifact(ProjectArtifactType artifactType, String text) {
		this.artifactType = artifactType;
		this.text = text;
	}

	public ProjectArtifactType getArtifactType() {
		return artifactType;
	}

	public String getText() {
		return text;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProjectArtifact that = (ProjectArtifact) o;
		return artifactType == that.artifactType && Objects.equals(text, that.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(artifactType, text);
	}

}

