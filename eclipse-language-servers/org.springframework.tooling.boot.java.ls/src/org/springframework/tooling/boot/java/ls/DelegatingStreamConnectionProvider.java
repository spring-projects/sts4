/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;

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
	private LanguageServer languageServer;
	
	private final IPropertyChangeListener configListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			sendConfiguration();
		}
	};
	
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
		BootJavaLanguageServerPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(configListener);
	}

	@Override
	public void handleMessage(Message message, LanguageServer languageServer, URI rootURI) {
		if (message instanceof ResponseMessage) {
			ResponseMessage responseMessage = (ResponseMessage)message;
			if (responseMessage.getResult() instanceof InitializeResult) {
				this.languageServer = languageServer;
				
				sendConfiguration();
				
				// Add config listener
				BootJavaLanguageServerPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(configListener);
				
				// Add resource listener
				ResourcesPlugin.getWorkspace().addResourceChangeListener(fResourceListener = new ResourceListener(languageServer, Arrays.asList(
						FileSystems.getDefault().getPathMatcher("glob:**/pom.xml"),
						FileSystems.getDefault().getPathMatcher("glob:**/*.gradle"),
						FileSystems.getDefault().getPathMatcher("glob:**/*.java")
				)));
			}
		}
	}
	
	private void sendConfiguration() {
		Map<String, Object> settings = new HashMap<>();
		Map<String, Object> bootJavaObj = new HashMap<>();
		Map<String, Object> bootHint = new HashMap<>();
		bootHint.put("on", BootJavaLanguageServerPlugin.getDefault().getPreferenceStore().getBoolean(Constants.PREF_BOOT_HINTS));
		bootJavaObj.put("boot-hints", bootHint);
		settings.put("boot-java", bootJavaObj);
		this.languageServer.getWorkspaceService().didChangeConfiguration(new DidChangeConfigurationParams(settings));
	}
	
}
