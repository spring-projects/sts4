/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.java.ls;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.osgi.framework.Bundle;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class SpringBootJavaLanguageServer extends ProcessStreamConnectionProvider {
	
	private ResourceListener fResourceListener;

	public SpringBootJavaLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(getJDKLocation());
		
//		commands.add("-Xdebug");
//		commands.add("-Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n");
		
		commands.add("-Dlsp.lazy.completions.disable=true");
		commands.add("-Dlsp.completions.indentation.enable=true");

		commands.add("-cp");
		commands.add(getToolsJAR() + ":" + getLanguageServerJARLocation());
		commands.add("org.springframework.boot.loader.JarLauncher");

		String workingDir = getWorkingDirLocation();

		setCommands(commands);
		setWorkingDirectory(workingDir);
	}
	
	protected String getJDKLocation() {
		File jre = new File(System.getProperty("java.home"));
		File javaExecutable = StandardVMType.findJavaExecutable(jre);
		return javaExecutable.getAbsolutePath();
	}
	
	protected String getToolsJAR() {
		File jre = new File(System.getProperty("java.home"));
		return new File(jre.getParent(), "lib" + Path.SEPARATOR + "tools.jar").getAbsolutePath();
	}
	
	protected String getLanguageServerJARLocation() {
		String fromSysprop = System.getProperty("boot-java-ls-jar", null);
		if (fromSysprop!=null) {
			return fromSysprop;
		}
		String languageServer = "boot-java-language-server-" + Constants.LANGUAGE_SERVER_VERSION;

		Bundle bundle = Platform.getBundle(Constants.PLUGIN_ID);
		String bundleVersion = bundle.getVersion().toString();

		String languageServerLocalCopy = bundleVersion + "-" + languageServer;
		
		File dataFile = bundle.getDataFile(languageServerLocalCopy);
		if (!dataFile.exists() || bundleVersion.endsWith("qualifier")) { // qualifier check to get the language server always copied in dev mode
			try {
				copyLanguageServerJAR(languageServer, languageServerLocalCopy);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return dataFile.getAbsolutePath();
	}
	
	protected String getWorkingDirLocation() {
		// TODO: identify a reasonable working directory for the language server process
		return System.getProperty("user.dir");
	}
	
	protected void copyLanguageServerJAR(String languageServerJarName, String languageServerLocalCopy) throws Exception {
		Bundle bundle = Platform.getBundle(Constants.PLUGIN_ID);
		InputStream stream = FileLocator.openStream( bundle, new Path("servers/" + languageServerJarName), false );
		
		File dataFile = bundle.getDataFile(languageServerLocalCopy);
		Files.copy(stream, dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	@Override
	public void stop() {
		super.stop();
		if (fResourceListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fResourceListener);
			fResourceListener = null;
		}
	}

	@Override
	public Object getInitializationOptions(URI rootPath) {
		installResourceChangeListener(rootPath);
		return super.getInitializationOptions(rootPath);
	}

	private void installResourceChangeListener(URI rootPath) {
		if (rootPath == null || fResourceListener != null) {
			return;
		}

		IContainer[] containers = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(rootPath);
		if (containers.length == 0) {
			return;
		}

		for (IContainer c : containers) {
			if (!(c instanceof IProject)) {
				continue;
			}
			IProject project = (IProject) c;
			fResourceListener = new ResourceListener("org.eclipse.languageserver.languages.springbootjava", project, Arrays.asList(
					FileSystems.getDefault().getPathMatcher("glob:**/pom.xml"),
					FileSystems.getDefault().getPathMatcher("glob:**/build.gradle")
			));
			project.getWorkspace().addResourceChangeListener(fResourceListener);

			break;
		}
	}

}
