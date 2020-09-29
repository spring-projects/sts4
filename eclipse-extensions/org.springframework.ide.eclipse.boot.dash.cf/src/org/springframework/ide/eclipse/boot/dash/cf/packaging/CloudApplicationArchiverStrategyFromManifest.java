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

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.ApplicationManifestHandler;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * Archiver strategy that consults manifest.yml file for an entry pointing to an existing archive.
 * The existing archive is returned by the archiver rather than building an archive.
 */
public class CloudApplicationArchiverStrategyFromManifest implements CloudApplicationArchiverStrategy {

//	private IProject project;
	private String applicationName;
	private ApplicationManifestHandler parser;

	public CloudApplicationArchiverStrategyFromManifest(IProject project, String applicationName, ApplicationManifestHandler parser) {
//		this.project = project;
		this.applicationName = applicationName;
		this.parser = parser;
	}

	@Override
	public ICloudApplicationArchiver getArchiver(IProgressMonitor mon) {
		final String archivePath = getArchivePath(mon);
		if (archivePath!=null) {
			return new ICloudApplicationArchiver() {
				public File getApplicationArchive(IProgressMonitor monitor) throws Exception {
					return getArchive(archivePath);
				}
			};
		}
		return null;
	}

	private String getArchivePath(IProgressMonitor mon) {
		if (parser.hasManifest()) {
			return parser.getApplicationProperty(applicationName, ApplicationManifestHandler.PATH_PROP, mon);
		}
		return null;
	}

	private File getArchive(String archivePath) throws Exception {
		Assert.isNotNull(archivePath);
		File packagedFile = null;
		// Only support paths that point to archive files
		IPath path = new Path(archivePath);
		if (path.getFileExtension() != null) {
			if (path.isAbsolute()) {
				// See if it is an absolute path
				File absoluteFile = new File(archivePath);
				if (absoluteFile.exists() && absoluteFile.canRead()) {
					packagedFile = absoluteFile;
				}
			} else {
				// We'll try and resolve the relative starting from the filesystem directory the manifest itself resides in.
				File manifestLocation = parser.getManifestFile();
				if (manifestLocation!=null) {
					File baseDir = manifestLocation.getParentFile();
					File absoluteFile = new File(baseDir, archivePath).getAbsoluteFile();
					if (absoluteFile.exists() && absoluteFile.canRead()) {
						packagedFile = absoluteFile;
					}
				}
			}
		}
		// If a path is specified but no file found stop further deployment
		if (packagedFile == null) {
			throw ExceptionUtil.coreException(
					"No file found at: " + path + ". Unable to package the application for deployment");
		} else {
			return packagedFile;
		}
	}

}