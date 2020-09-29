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

import java.io.File;

import org.eclipse.compare.CompareConfiguration;

/**
 * The result from the compare model that contains two sides: a local project
 * and a downloaded project
 *
 */
public class AddStartersCompareResult {

	private final LocalProject localResource;
	private File downloadedProject;

	public AddStartersCompareResult(LocalProject localResource, File downloadedProject) {
		this.localResource = localResource;
		this.downloadedProject = downloadedProject;
	}

	public File getDownloadedProject() {
		return downloadedProject;
	}

	public LocalProject getLocalResource() {
		return localResource;
	}

	public CompareConfiguration getConfiguration() {
		CompareConfiguration config = new CompareConfiguration();
		config.setLeftLabel("Spring Initializr: " + downloadedProject.getName());
		config.setRightLabel(localResource.getLabel());
		config.setRightEditable(localResource.isEditable());
		return config;
	}

}
