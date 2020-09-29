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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.boot.loader.tools.JarWriter;
import org.springframework.boot.loader.tools.Libraries;
import org.springframework.boot.loader.tools.Library;
import org.springframework.boot.loader.tools.LibraryCallback;
import org.springframework.boot.loader.tools.LibraryScope;
import org.springframework.boot.loader.tools.Repackager;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.util.JavaProjectUtil;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.frameworks.core.maintype.MainTypeFinder;
import org.springsource.ide.eclipse.commons.frameworks.core.util.FileUtil;

public class CloudApplicationArchiverStrategyAsJar implements CloudApplicationArchiverStrategy {

	private static final String TEMP_FOLDER_NAME = "springidetempFolderForJavaAppJar";
	private static final boolean DEBUG = false;

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	/**
	 * Classpath entries spilt into two lists, one that correspond to the current project's output folders
	 * and all the others (which correspond to the project's dependencies). The dependencies could be
	 * jars or output folders for other projects in the workspace.
	 */
	private static class SplitClasspath {
		private List<File> projectContents = new ArrayList<>(2); //one or two is typical
		private List<File> dependencies = new ArrayList<>();
		public SplitClasspath(IJavaProject jp, File[] entries) {
			Set<File> outputFolders = toFileSet(JavaProjectUtil.getOutputFolders(jp));
			for (File file : entries) {
				if (contains(outputFolders, file)) {
					projectContents.add(file);
				} else {
					dependencies.add(file);
				}
			}
		}

		private boolean contains(Set<File> outputFolders, File file) {
			return outputFolders.contains(canonical(file));
		}

		private File canonical(File file) {
			try {
				return file.getCanonicalFile();
			} catch (IOException e) {
				//Next best thing:
				return file.getAbsoluteFile();
			}
		}

		/**
		 * Convert a collection of Eclipse IContainer to List of java.io.File. Containers that
		 * don't correspond to stuff on disk are silently ignored.
		 */
		private Set<File> toFileSet(Set<IContainer> containers) {
			Set<File> files = new HashSet<>();
			for (IContainer folder : containers) {
				IPath loc = folder.getLocation();
				if (loc!=null) {
					File file = loc.toFile();
					files.add(canonical(file)); //use canonical file to make equals / Set work as expected.
				}
			}
			return files;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder("SplitClasspath(\n");
			for (File file : projectContents) {
				builder.append("  "+file+"\n");
			}
			builder.append("   ------------\n");
			for (File file : dependencies) {
				builder.append("  "+file+"\n");
			}
			builder.append(")");
			return builder.toString();
		}
	}



	private static final File[] NO_FILES = new File[]{};

	private static class Archiver implements ICloudApplicationArchiver {

		private IJavaProject jp;
		private IType mainType;
		private ILaunchConfiguration conf;
		private BootLaunchConfigurationDelegate delegate;
		private JarNameGenerator jarNames;
		private File _tempFolder;

		Archiver(IJavaProject jp, IType mainType) throws CoreException {
			this.jp = jp;
			this.mainType = mainType;
			this.conf = BootLaunchConfigurationDelegate.createWorkingCopy(mainType);
			this.delegate = new BootLaunchConfigurationDelegate();
			this.jarNames = new JarNameGenerator();
		}

		private SplitClasspath getRuntimeClasspath() throws CoreException {
			return new SplitClasspath(jp, toFiles(delegate.getClasspath(conf)));
		}

		private File[] toFiles(String[] classpath) {
			if (classpath!=null) {
				File[] files = new File[classpath.length];
				for (int i = 0; i < files.length; i++) {
					files[i] = new File(classpath[i]);
				}
				return files;
			}
			return NO_FILES;
		}

		@Override
		public File getApplicationArchive(IProgressMonitor mon) throws Exception {
			SplitClasspath classpath = getRuntimeClasspath();
			File tempFolder = getTempFolder();
			File baseJar = new File(tempFolder, jp.getElementName()+".original.jar");
			File repackagedJar = new File(tempFolder, jp.getElementName()+".repackaged.jar");

			createBaseJar(classpath.projectContents, baseJar);
			repackage(baseJar, classpath.dependencies, repackagedJar);
			return repackagedJar;
		}

		private File getTempFolder() throws IOException {
			if (_tempFolder==null) {
				_tempFolder = FileUtil.createTempDirectory(TEMP_FOLDER_NAME);
			}
			return _tempFolder;
		}

		private void createBaseJar(List<File> projectContents, File baseJar) throws FileNotFoundException, IOException {
			JarWriter jarWriter = new JarWriter(baseJar);
			try {
				for (File outputFolder : projectContents) {
					writeFolder(jarWriter, outputFolder);
				}
			} finally {
				jarWriter.close();
			}
		}

		private void writeFolder(JarWriter jarWriter, File baseFolder) throws FileNotFoundException, IOException {
			for (String name : baseFolder.list()) {
				write(jarWriter, baseFolder, name);
			}
		}

		private void write(JarWriter jarWriter, File baseFolder, String relativePath) throws FileNotFoundException, IOException {
			debug("Writing: "+relativePath + " from "+baseFolder);
			File file = new File(baseFolder, relativePath);
			if (file.isDirectory()) {
				debug("Folder");
				for (String name : file.list()) {
					write(jarWriter, baseFolder, pathJoin(relativePath, name));
				}
			} else if (file.isFile()) {
				debug("File");
				jarWriter.writeEntry(relativePath, new FileInputStream(file));
			} else {
				debug("Huh?");
			}
		}

		private String pathJoin(String relativePath, String name) {
			return relativePath + "/" +name;
		}

		private void repackage(File baseJar, List<File> dependencies, File repackagedJar) throws IOException {
			Repackager repackager = new Repackager(baseJar);
			repackager.setMainClass(mainType.getFullyQualifiedName());
			repackager.repackage(repackagedJar, asLibraries(dependencies));
		}

		private Libraries asLibraries(final List<File> dependencies) {
			return new Libraries() {
				public void doWithLibraries(LibraryCallback callback) throws IOException {
					for (File dep : dependencies) {
						if (dep.isFile()) {
							callback.library(new Library(jarNames.createName(dep), dep, LibraryScope.COMPILE, false));
						} else if (dep.isDirectory()) {
							String jarName = jarNames.createName(dep);
							File jarFile = new File(getTempFolder(), jarName);
							JarWriter jarWriter = new JarWriter(jarFile);
							try {
								writeFolder(jarWriter, dep);
							} finally {
								jarWriter.close();
							}
							callback.library(new Library(jarName, jarFile, LibraryScope.COMPILE, false));
						}
					}
				}
			};
		}
	}

	private SpringBootCore springBootCore = SpringBootCore.getDefault();
	private IProject project;
	private UserInteractions ui;

	public CloudApplicationArchiverStrategyAsJar(IProject project, UserInteractions ui) {
		this.project = project;
		this.ui = ui;
	}

	@Override
	public ICloudApplicationArchiver getArchiver(IProgressMonitor mon) {
		try {
			final IJavaProject jp = getJavaProject();
			if (jp!=null && checkPackagingType(jp)) {
				final IType type = getMainType(jp, mon);
				if (type!=null) {
					return new Archiver(jp, type);
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	private boolean checkPackagingType(IJavaProject jp) throws CoreException {
		ISpringBootProject bootProject = springBootCore.project(jp);
		if (bootProject==null) {
			//Gradle is poorly supported. We don't know how to determin packaging type. So just
			// give such projects the benefit of the doubdt. They *might have the correct
			// packaging type.
			return true;
		}
		return ISpringBootProject.PACKAGING_JAR.equals(bootProject.getPackaging());
	}

	private IJavaProject getJavaProject() {
		try {
			if (project.isAccessible() && project.hasNature(JavaCore.NATURE_ID)) {
				return JavaCore.create(project);
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	private IType getMainType(IJavaProject jp, IProgressMonitor mon) {
		try {
			IType[] candidates = MainTypeFinder.guessMainTypes(jp, mon);
			if (candidates!=null && candidates.length>0) {
				if (candidates.length==1) {
					return candidates[0];
				} else {
					//TODO: should persist main type so we don't ask again next time.
					// however we persist this, user must be able to change it.
					// Prolly we should create a launchconf to store info like this and
					// create UI for user to modify it.
					return ui.chooseMainType(candidates, "Choose a main type", "Deploying a standalone boot-app requires "
							+ "that the main type is identified. We found several candidates, please choose one." );
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

}
