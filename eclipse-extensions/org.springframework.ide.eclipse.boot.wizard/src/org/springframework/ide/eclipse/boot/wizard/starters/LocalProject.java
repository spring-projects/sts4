/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import org.eclipse.core.resources.IProject;

public class LocalProject {
	private String label;
	private boolean editable;
	private final IProject project;

	public LocalProject(IProject project, boolean editable) {
		this.project = project;
		this.label = "Local project: " + project.getName();
		this.editable = editable;
	}

	public String getLabel() {
		return label;
	}

	public IProject getProject() {
		return project;
	}

	public boolean isEditable() {
		return editable;
	}
}
