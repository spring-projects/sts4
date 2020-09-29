/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.packaging;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

/**
 * Some utilities for creating {@link CloudApplicationArchiverStrategy} instances.
 *
 * @author Kris De Volder
 */
public class CloudApplicationArchiverStrategies {

	public static CloudApplicationArchiverStrategy justReturn(final ICloudApplicationArchiver archiver) {
		return new CloudApplicationArchiverStrategy() {
			@Override
			public ICloudApplicationArchiver getArchiver(IProgressMonitor mon) {
				return archiver;
			}
		};
	}

	public static CloudApplicationArchiverStrategy fromManifest(IProject project, String appName, ApplicationManifestHandler parser) {
		return new CloudApplicationArchiverStrategyFromManifest(project, appName, parser);
	}

	public static CloudApplicationArchiverStrategy packageAsJar(IProject project, UserInteractions ui) {
		return new CloudApplicationArchiverStrategyAsJar(project, ui);
	}

	public static CloudApplicationArchiverStrategy packageMvnAsWar(IProject project, UserInteractions ui) {
		return new CloudApplicationArchiverStrategyMavenAsWar(project, ui);
	}

}
