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

