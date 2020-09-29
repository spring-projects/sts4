/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.packaging;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.util.Log;

/**
 * Package a boot app to deploy it as a 'war'.
 *
 * @author Kris De Volder
 */
public class CloudApplicationArchiverStrategyMavenAsWar implements CloudApplicationArchiverStrategy {

	private SpringBootCore springBootCore = SpringBootCore.getDefault();
	private IProject project;
//	private UserInteractions ui;

	public CloudApplicationArchiverStrategyMavenAsWar(IProject project, UserInteractions ui) {
		this.project = project;
//		this.ui = ui;
	}

	@Override
	public ICloudApplicationArchiver getArchiver(IProgressMonitor mon) {
		try {
			ISpringBootProject bootProject = springBootCore.project(project);
			if (bootProject!=null && ISpringBootProject.PACKAGING_WAR.equals(bootProject.getPackaging())) {
				return new WarArchiver();
			}
		} catch (CoreException e) {
			Log.log(e);
		}
		return null;
	}

	private class WarArchiver implements ICloudApplicationArchiver {

		@Override
		public File getApplicationArchive(IProgressMonitor monitor) throws Exception {
			ISpringBootProject bootProject = springBootCore.project(project);
			if (bootProject!=null) {
				return bootProject.executePackagingScript(monitor);
			}
			return null;
		}

	}

}
