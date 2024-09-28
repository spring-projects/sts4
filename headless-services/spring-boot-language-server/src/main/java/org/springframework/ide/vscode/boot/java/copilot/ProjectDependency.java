package org.springframework.ide.vscode.boot.java.copilot;

public class ProjectDependency {

	private final String groupId;

	private final String artifactId;

	public ProjectDependency(String groupId, String artifactId) {
		this.groupId = groupId;
		this.artifactId = artifactId;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	@Override
	public String toString() {
		return "ProjectDependency{" + "groupId='" + groupId + '\'' + ", artifactId='" + artifactId + '\'' + '}';
	}

}
