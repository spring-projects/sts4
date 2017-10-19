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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.Arrays;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.lsp4e.server.StreamConnectionProvider;

/**
 * if the system property "boot-java-ls-port" exists, delegate to the socket-based
 * stream connection provider, otherwise use the standard process-based stream
 * connection provider.
 * 
 * This allows you to run the language server in server mode (listens on a port for
 * a connection) and connect the IDE integration to that already running language
 * server instead of starting a new process for it.
 * 
 * @author Martin Lippert
 */
public class DelegatingStreamConnectionProvider implements StreamConnectionProvider {
	
	private StreamConnectionProvider provider;
	private ResourceListener fResourceListener;
	
	public DelegatingStreamConnectionProvider() {
		String port = System.getProperty("boot-java-ls-port");
		
		if (port != null) {
			this.provider = new SpringBootJavaLanguageServerViaSocket(Integer.parseInt(port));
		}
		else {
			this.provider = new SpringBootJavaLanguageServer();
		}
	}
		
	@Override
	public Object getInitializationOptions(URI rootUri) {
		installResourceChangeListener(rootUri);
		return provider.getInitializationOptions(rootUri);
	}

	@Override
	public void start() throws IOException {
		this.provider.start();
	}

	@Override
	public InputStream getInputStream() {
		return this.provider.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() {
		return this.provider.getOutputStream();
	}

	@Override
	public void stop() {
		this.provider.stop();
		if (fResourceListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fResourceListener);
			fResourceListener = null;
		}
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
