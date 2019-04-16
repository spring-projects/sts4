/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.springframework.tooling.ls.eclipse.commons.LanguageServerCommonsActivator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import com.google.common.collect.ImmutableSet;

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
	
	private final IPropertyChangeListener configListener = (e) -> sendConfiguration();
	private final ValueListener<ImmutableSet<Object>> remoteAppsListener = (e, v) -> sendConfiguration();
	
	private long timestampBeforeStart;
	private long timestampWhenInitialized;
	
	public DelegatingStreamConnectionProvider() {
		LanguageServerCommonsActivator.logInfo("Entering DelegatingStreamConnectionProvider()");
		String port = System.getProperty("boot-java-ls-port");
		if (port != null) {
			this.provider = new SpringBootLanguageServerViaSocket(Integer.parseInt(port));
		} else {
			LanguageServerCommonsActivator.logInfo("DelegatingStreamConnectionProvider classloader = "+this.getClass().getClassLoader());
			Assert.isNotNull(SpringBootLanguageServer.class);
			LanguageServerCommonsActivator.logInfo("SpringBootLanguageServer exists!");
			Assert.isLegal(SpringBootLanguageServer.class.getClassLoader().equals(DelegatingStreamConnectionProvider.class.getClassLoader()), "OSGI bug messing up our classloaders?");
			this.provider = new SpringBootLanguageServer();
		}
	}
		
	@Override
	public Object getInitializationOptions(URI rootUri) {
		return provider.getInitializationOptions(rootUri);
	}

	@Override
	public void start() throws IOException {
		this.timestampBeforeStart = System.currentTimeMillis();
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
	public InputStream getErrorStream() {
		return provider.getErrorStream();
	}

	@Override
	public void stop() {
		this.provider.stop();
		if (fResourceListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fResourceListener);
			fResourceListener = null;
		}
		BootLanguageServerPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(configListener);
		BootLanguageServerPlugin.getRemoteBootApps().removeListener(remoteAppsListener);
	}

	@Override
	public void handleMessage(Message message, LanguageServer languageServer, URI rootURI) {
		if (message instanceof ResponseMessage) {
			ResponseMessage responseMessage = (ResponseMessage)message;
			if (responseMessage.getResult() instanceof InitializeResult) {
				this.languageServer = languageServer;
				
				this.timestampWhenInitialized = System.currentTimeMillis();
				LanguageServerCommonsActivator.logInfo("Boot LS startup time from start to initialized: " + (timestampWhenInitialized - timestampBeforeStart) + "ms");
				
				sendConfiguration();
				
				// Add config listener
				BootLanguageServerPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(configListener);
				
				// Add resource listener
				ResourcesPlugin.getWorkspace().addResourceChangeListener(fResourceListener = new ResourceListener(languageServer, Arrays.asList(
						FileSystems.getDefault().getPathMatcher("glob:**/pom.xml"),
						FileSystems.getDefault().getPathMatcher("glob:**/*.xml"),
						FileSystems.getDefault().getPathMatcher("glob:**/*.gradle"),
						FileSystems.getDefault().getPathMatcher("glob:**/*.java"),
						FileSystems.getDefault().getPathMatcher("glob:**/*.json"),
						FileSystems.getDefault().getPathMatcher("glob:**/*.yml"),
						FileSystems.getDefault().getPathMatcher("glob:**/*.properties")
				)));
				
				//Add remote boot apps listener
				BootLanguageServerPlugin.getRemoteBootApps().addListener(remoteAppsListener);
			}
		}
	}
	
	public class RemoteBootAppData {
		
		private String jmxurl;
		private String host;
		private String urlScheme = "https";
		private String port = "443";
		private boolean keepChecking = true; 
			//keepChecking defaults to true. Boot dash automatic remote apps should override this explicitly.
			//Reason. All other 'sources' of remote apps are 'manual' and we want them to default to
			//'keepChecking' even if the user doesn't set this to true manually.

		public RemoteBootAppData(String jmxurl, String host) {
			super();
			this.jmxurl = jmxurl;
			this.host = host;
		}

		public String getJmxurl() {
			return jmxurl;
		}

		public void setJmxurl(String jmxurl) {
			this.jmxurl = jmxurl;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public String getUrlScheme() {
			return urlScheme;
		}

		public void setUrlScheme(String urlScheme) {
			this.urlScheme = urlScheme;
		}

		public String getPort() {
			return port;
		}

		public void setPort(String port) {
			this.port = port;
		}
		
		public boolean isKeepChecking() {
			return keepChecking;
		}
		
		public void setKeepChecking(boolean keepChecking) {
			this.keepChecking = keepChecking;
		}
	}
	
	private void sendConfiguration() {
		Map<String, Object> settings = new HashMap<>();
		Map<String, Object> bootJavaObj = new HashMap<>();
		Map<String, Object> bootHint = new HashMap<>();
		Map<String, Object> supportXML = new HashMap<>();
		Map<String, Object> bootChangeDetection = new HashMap<>();

		bootHint.put("on", BootLanguageServerPlugin.getDefault().getPreferenceStore().getBoolean(Constants.PREF_BOOT_HINTS));
		supportXML.put("on", BootLanguageServerPlugin.getDefault().getPreferenceStore().getBoolean(Constants.PREF_SUPPORT_SPRING_XML_CONFIGS));
		bootChangeDetection.put("on", BootLanguageServerPlugin.getDefault().getPreferenceStore().getBoolean(Constants.PREF_CHANGE_DETECTION));

		bootJavaObj.put("boot-hints", bootHint);
		bootJavaObj.put("support-spring-xml-config", supportXML);
		bootJavaObj.put("change-detection", bootChangeDetection);

		bootJavaObj.put("remote-apps", BootLanguageServerPlugin.getRemoteBootApps().getValues()
				.stream()
				.map(this::parseData)
				.collect(Collectors.toList())
		);

		settings.put("boot-java", bootJavaObj);

		this.languageServer.getWorkspaceService().didChangeConfiguration(new DidChangeConfigurationParams(settings));
	}
	
	@SuppressWarnings("unchecked")
	private RemoteBootAppData parseData(Object incomingData) {
		if (incomingData instanceof Pair) {
			//Format prior to STS 4.2.0. Still supported to allows STS 3.9.8 and older to
			// send data from its boot dash in the old format.
			Pair<String,String> pair = (Pair<String, String>) incomingData;
			return new RemoteBootAppData(pair.getLeft(), pair.getRight());
		} else if (incomingData instanceof List) {
			//Format since STS 4.2.0
			List<String> list = (List<String>) incomingData;
			RemoteBootAppData app = new RemoteBootAppData(list.get(0), list.get(1));
			if (list.size()>=3) {
				String portStr = list.get(2);
				if (portStr!=null) {
					app.setPort(portStr);
				}
			}
			if (list.size()>=4) {
				String urlScheme = list.get(3);
				if (urlScheme!=null) {
					app.setUrlScheme(urlScheme);
				}
			}
			//keepChecking attribute added in STS 4.2.1
			if (list.size()>=5) {
				String keepChecking = list.get(4);
				app.setKeepChecking("true".equals(keepChecking));
			}
			return app;
		}
		throw new IllegalArgumentException("Invalid remote app data: "+incomingData);
	}

}
