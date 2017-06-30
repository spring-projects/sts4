/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.cloudfoundry.manifest.ls;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.osgi.framework.Bundle;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class CloudFoundryManifestLanguageServer extends ProcessStreamConnectionProvider {
	
	private static LanguageServerProcessReaper processReaper = new LanguageServerProcessReaper();
	
	private LanguageServer languageServer;
	private URI rootPath;

	private static Object cfTargetOptionSettings = null;
	private static List<CloudFoundryManifestLanguageServer> servers = new CopyOnWriteArrayList<>();

	public CloudFoundryManifestLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(getJDKLocation());
		commands.add("-jar");
		commands.add(getLanguageServerJARLocation());

		String workingDir = getWorkingDirLocation();

//		commands.add("-Xdebug");
//		commands.add("-agentlib:jdwp=transport=dt_socket,address=9999,server=y,suspend=n");

		setCommands(commands);
		setWorkingDirectory(workingDir);
	}
	
	@Override
	public void handleMessage(Message message, LanguageServer languageServer, URI rootPath) {
		if (message instanceof ResponseMessage) {
			ResponseMessage responseMessage = (ResponseMessage)message;
			if (responseMessage.getResult() instanceof InitializeResult) {
				this.languageServer = languageServer;
				this.rootPath = rootPath;
				
				updateLanguageServer();
				addLanguageServer(this);
			}
		}
	}
	
	public static void setCfTargetLoginOptions(Object cfTargetOptions) {
		cfTargetOptionSettings = cfTargetOptions;
		
		for (CloudFoundryManifestLanguageServer server : servers) {
			server.updateLanguageServer();
		}
	}

	@Override
	public void stop() {
		removeLanguageServer(this);
		super.stop();
		processReaper.removeProcess(this.getProcess());
	}
	
	@Override
	public Object getInitializationOptions(URI rootUri) {
		return cfTargetOptionSettings;
	}
	
	protected String getJDKLocation() {
		try {
			File javaHome= new File(System.getProperty("java.home")).getCanonicalFile(); //$NON-NLS-1$
			if (javaHome.exists()) {
				File javaExecutable = StandardVMType.findJavaExecutable(javaHome);
				if (javaExecutable != null && javaExecutable.exists()) {
					return javaExecutable.getAbsolutePath();
				}
			}
		} catch (IOException e) {
			return null;
		}
		
		return null;
	}
	
	@Override
	public void start() throws IOException {
		super.start();
		processReaper.addProcess(getProcess());
	}
	
	private Process getProcess() {
		try {
			//The super class is doesn't provide a way to get at the process without using reflection...
			// This method can be removed if / when the super-class provides a getProcess method we can call.
			Field processField = ProcessStreamConnectionProvider.class.getDeclaredField("process");
			processField.setAccessible(true);
			Process process = (Process) processField.get(this);
			return process;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
	}

	protected String getLanguageServerJARLocation() {
		String languageServer = "manifest-yaml-language-server-" + Constants.LANGUAGE_SERVER_VERSION;

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

	protected void updateLanguageServer() {
		DidChangeConfigurationParams params = new DidChangeConfigurationParams(getInitializationOptions(rootPath));
		languageServer.getWorkspaceService().didChangeConfiguration(params);
	}

	private static void addLanguageServer(CloudFoundryManifestLanguageServer server) {
		servers.add(server);
	}

	private static void removeLanguageServer(CloudFoundryManifestLanguageServer server) {
		servers.remove(server);
	}

}
