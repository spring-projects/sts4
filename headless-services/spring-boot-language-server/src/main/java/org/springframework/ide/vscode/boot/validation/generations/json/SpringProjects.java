/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations.json;

import java.util.List;

public class SpringProjects {
	private List<SpringProject> projects;

	public List<SpringProject> getProjects() {
		return projects;
	}

	public void setProjects(List<SpringProject> projects) {
		this.projects = projects;
	}
}