package org.springframework.ide.vscode.boot.java.copilot;

import java.util.ArrayList;
import java.util.List;

public class ProcessArtifactResult<T> {

	List<ProjectArtifact> notProcessedArtifacts = new ArrayList<>();

	private T result;

	public List<ProjectArtifact> getNotProcessedArtifacts() {
		return notProcessedArtifacts;
	}

	public void addToNotProcessed(ProjectArtifact projectArtifact) {
		notProcessedArtifacts.add(projectArtifact);
	}

	public T getResult() {
		return result;
	}

	void setResult(T result) {
		this.result = result;
	}

}

