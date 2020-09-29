/*******************************************************************************
 * Copyright (c) 2015, 2018 Pivotal, Inc.
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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.springframework.boot.loader.tools.Libraries;
import org.springframework.boot.loader.tools.Library;
import org.springframework.boot.loader.tools.LibraryCallback;
import org.springframework.boot.loader.tools.LibraryScope;
import org.springframework.boot.loader.tools.Repackager;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.JavaPackageFragmentRootHandler;
import org.springframework.ide.eclipse.boot.dash.util.UiUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.util.FileUtil;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class CloudApplicationArchiver implements ICloudApplicationArchiver {

	private IJavaProject javaProject;

	private String applicationName;

	private final ApplicationManifestHandler parser;

	private static final String TEMP_FOLDER_NAME = "springidetempFolderForJavaAppJar";

	public CloudApplicationArchiver(IJavaProject javaProject, String applicationName,
			ApplicationManifestHandler parser) {
		this.javaProject = javaProject;
		this.applicationName = applicationName;
		this.parser = parser;
	}

	public File getApplicationArchive(IProgressMonitor monitor) throws Exception {
		File archive = getArchiveFromManifest(monitor);
		if (archive == null) {

			File packagedFile = null;

			JavaPackageFragmentRootHandler rootResolver = getPackageFragmentRootHandler(javaProject, monitor);

			final IPackageFragmentRoot[] roots = rootResolver.getPackageFragmentRoots(monitor);

			if (roots == null || roots.length == 0) {
				throw ExceptionUtil.coreException("Unable to package project" + javaProject.getElementName()
						+ " as a jar application. Please verify that the project is a valid Java project and contains a main type in source.");
			}

			IType mainType = rootResolver.getMainType(monitor);

			JarPackageData jarPackageData = getJarPackageData(roots, mainType, monitor);

			// generate a manifest file. Note that manifest files
			// are only generated in the temporary jar meant for
			// deployment.
			// The associated Java project is no modified.
			jarPackageData.setGenerateManifest(true);

			// This ensures that folders in output folders appear at root
			// level
			// Example: src/main/resources, which is in the project's
			// classpath, contains non-Java templates folder and
			// has output folder target/classes. If not exporting output
			// folder,
			// templates will be packaged in the jar using this path:
			// resources/templates
			// This may cause problems with the application's dependencies
			// if they are looking for just /templates at top level of the
			// jar
			// If exporting output folders, templates folder will be
			// packaged at top level in the jar.
			jarPackageData.setExportOutputFolders(true);

			packagedFile = packageApplication(jarPackageData, monitor);

			bootRepackage(roots, packagedFile);

			archive = packagedFile;
		}

		return archive;
	}

	protected JavaPackageFragmentRootHandler getPackageFragmentRootHandler(IJavaProject javaProject,
			IProgressMonitor monitor) throws CoreException {

		return new JavaPackageFragmentRootHandler(javaProject);
	}

	protected void bootRepackage(final IPackageFragmentRoot[] roots, File packagedFile) throws Exception {
		Repackager bootRepackager = new Repackager(packagedFile);
		bootRepackager.repackage(new Libraries() {

			public void doWithLibraries(LibraryCallback callBack) throws IOException {
				for (IPackageFragmentRoot root : roots) {

					if (root.isArchive()) {

						File rootFile = new File(root.getPath().toOSString());
						if (rootFile.exists()) {
							callBack.library(new Library(rootFile, LibraryScope.COMPILE));
						}
					}
				}
			}
		});
	}

	protected JarPackageData getJarPackageData(IPackageFragmentRoot[] roots, IType mainType, IProgressMonitor monitor)
			throws Exception {

		String filePath = getTempJarPath();

		IPath location = new Path(filePath);

		// Note that if no jar builder is specified in the package data
		// then a default one is used internally by the data that does NOT
		// package any jar dependencies.
		JarPackageData packageData = new JarPackageData();

		packageData.setJarLocation(location);

		// Don't create a manifest. A repackager should determine if a generated
		// manifest is necessary
		// or use a user-defined manifest.
		packageData.setGenerateManifest(false);

		// Since user manifest is not used, do not save to manifest (save to
		// manifest saves to user defined manifest)
		packageData.setSaveManifest(false);

		packageData.setManifestMainClass(mainType);
		packageData.setElements(roots);
		return packageData;
	}

	protected File packageApplication(final JarPackageData packageData, IProgressMonitor monitor) throws Exception {

		int progressWork = 10;
		final SubMonitor subProgress = SubMonitor.convert(monitor, progressWork);

		final File[] createdFile = new File[1];

		final Exception[] error = new Exception[1];
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = UiUtil.getShell();

				IJarExportRunnable runnable = packageData.createJarExportRunnable(shell);
				try {
					runnable.run(subProgress);

					File file = new File(packageData.getJarLocation().toString());
					createdFile[0] = file;

				} catch (InvocationTargetException e) {
					error[0] = e;
				} catch (InterruptedException ie) {
					error[0] = ie;
				} finally {
					subProgress.done();
				}
			}

		});
		if (error[0] != null) {
			throw error[0];
		}

		return createdFile[0];
	}

	public String getTempJarPath() throws Exception {
		File tempFolder = FileUtil.createTempDirectory(TEMP_FOLDER_NAME);
		tempFolder.delete();
		tempFolder.mkdirs();

		if (!tempFolder.exists()) {
			throw ExceptionUtil.coreException("Failed to create temporary jar file when packaging application for deployment: "
							+ tempFolder.getAbsolutePath());
		}

		File targetFile = new File(tempFolder, applicationName + ".jar");

		String path = new Path(targetFile.getAbsolutePath()).toString();

		System.out.println("getTempJarPath => "+path);
		return path;
	}

	public File getArchiveFromManifest(IProgressMonitor monitor) throws Exception {
		String archivePath = null;
		// Read the path again instead of deployment info, as a user may be
		// correcting the path after the module was creating and simply
		// attempting to push it again without the
		// deployment wizard
		if (parser.hasManifest()) {
			archivePath = parser.getApplicationProperty(applicationName, ApplicationManifestHandler.PATH_PROP, monitor);
		}

		File packagedFile = null;
		if (archivePath != null) {
			// Only support paths that point to archive files
			IPath path = new Path(archivePath);
			if (path.getFileExtension() != null) {

				if (!path.isAbsolute()) {
					// Check if it is project relative first
					IFile projectRelativeFile = javaProject.getProject().getFile(path);
					if (projectRelativeFile != null && projectRelativeFile.exists()) {
						packagedFile = projectRelativeFile.getLocation().toFile();
					} else {
						// Case where file exists in file system but is not
						// present in workspace (i.e. IProject may be out of
						// synch with file system)
						IPath projectPath = javaProject.getProject().getLocation();
						if (projectPath != null) {
							archivePath = projectPath.append(archivePath).toString();
							File absoluteFile = new File(archivePath);
							if (absoluteFile.exists() && absoluteFile.canRead()) {
								packagedFile = absoluteFile;
							}
						}
					}
				} else {
					// See if it is an absolute path
					File absoluteFile = new File(archivePath);
					if (absoluteFile.exists() && absoluteFile.canRead()) {
						packagedFile = absoluteFile;
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
		return null;
	}
}