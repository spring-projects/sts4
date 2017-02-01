/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.boot.ide.properties.servers;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.NotificationMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.osgi.framework.Bundle;

import com.google.gson.JsonObject;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class SpringBootPropertiesLanguageServer extends ProcessStreamConnectionProvider {

	public SpringBootPropertiesLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(getJDKLocation());
		commands.add("-jar");
		commands.add(getLanguageServerJARLocation());

		String workingDir = getWorkingDirLocation();

		setCommands(commands);
		setWorkingDirectory(workingDir);
	}
	
	public void handleMessage(Message message, LanguageServer languageServer, String rootPath) {
		if (message instanceof NotificationMessage) {
			NotificationMessage notificationMessage = (NotificationMessage) message;
			if ("sts/progress".equals(notificationMessage.getMethod())) {
				JsonObject params = (JsonObject) notificationMessage.getParams();
				if (params.has("statusMsg")) {
					String status = params.get("statusMsg").getAsString();
					System.out.println("STS4 Language Server Status Update: " + status);
				}
				else {
					System.out.println("STS4 Language Server Status Update: DONE");
				}
			}
		}
	}
	
	protected String getJDKLocation() {
		IVMInstall jdk = JavaRuntime.getDefaultVMInstall();
		File javaExecutable = StandardVMType.findJavaExecutable(jdk.getInstallLocation());
		return javaExecutable.getAbsolutePath();
	}
	
	protected String getLanguageServerJARLocation() {
		String languageServer = "vscode-boot-properties-0.0.1-SNAPSHOT.jar";

		Bundle bundle = Platform.getBundle(Constants.PLUGIN_ID);
		File dataFile = bundle.getDataFile(languageServer);
		if (!dataFile.exists()) {
			try {
				copyLanguageServerJAR(languageServer);
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
	
	protected void copyLanguageServerJAR(String languageServerJarName) throws Exception {
		Bundle bundle = Platform.getBundle(Constants.PLUGIN_ID);
		InputStream stream = FileLocator.openStream( bundle, new Path("servers/" + languageServerJarName), false );
		
		File dataFile = bundle.getDataFile(languageServerJarName);
		Files.copy(stream, dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

}
