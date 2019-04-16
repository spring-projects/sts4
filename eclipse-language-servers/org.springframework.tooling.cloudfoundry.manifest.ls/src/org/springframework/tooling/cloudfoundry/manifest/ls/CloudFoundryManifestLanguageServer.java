/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.cloudfoundry.manifest.ls;

import static org.springframework.tooling.ls.eclipse.commons.preferences.LanguageServerConsolePreferenceConstants.CLOUDFOUNDRY_SERVER;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.osgi.framework.Bundle;
import org.springframework.tooling.ls.eclipse.commons.JRE;
import org.springframework.tooling.ls.eclipse.commons.STS4LanguageServerProcessStreamConnector;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class CloudFoundryManifestLanguageServer extends STS4LanguageServerProcessStreamConnector {
	
	private LanguageServer languageServer;
	private URI rootPath;

	private static Object cfTargetOptionSettings = null;
	private static List<CloudFoundryManifestLanguageServer> servers = new CopyOnWriteArrayList<>();

	public CloudFoundryManifestLanguageServer() {
		super(CLOUDFOUNDRY_SERVER);
		setCommands(JRE.currentJRE().jarLaunchCommand(getLanguageServerJARLocation(), ImmutableList.of(
				//"-Xdebug",
				//"-agentlib:jdwp=transport=dt_socket,address=8899,server=y,suspend=n",
				"-Dlsp.lazy.completions.disable=true",
				"-Dlsp.completions.indentation.enable=true"
		)));
		setWorkingDirectory(getWorkingDirLocation());
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
	}
	
	@Override
	public Object getInitializationOptions(URI rootUri) {
		return cfTargetOptionSettings;
	}
	
	protected String getLanguageServerJARLocation() {
		String languageServer = "manifest-yaml-language-server-" + Constants.LANGUAGE_SERVER_VERSION;

		Bundle bundle = Platform.getBundle(Constants.PLUGIN_ID);
		String bundleVersion = bundle.getVersion().toString();

		String languageServerLocalCopy = bundleVersion + "-" + languageServer;
		
		File dataFile = bundle.getDataFile(languageServerLocalCopy);

		Exception error = null;
		if (!dataFile.exists() || bundleVersion.endsWith("qualifier")) { // qualifier check to get the language server always copied in dev mode
			try {
				copyLanguageServerJAR(languageServer, languageServerLocalCopy);
			}
			catch (Exception e) {
				error = e;
			}
		}
		
		if (bundleVersion.endsWith("qualifier")) {
			File userHome = new File(System.getProperty("user.home"));
			File locallyBuiltJar = new File(
					userHome, 
					"git/sts4/headless-services/manifest-yaml-language-server/target/manifest-yaml-language-server-" + Constants.LANGUAGE_SERVER_VERSION
			);
			if (locallyBuiltJar.exists()) {
				return locallyBuiltJar.getAbsolutePath();
			}
			if (error != null) {
				error.printStackTrace();
			}
		}
		return dataFile.getAbsolutePath();
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
