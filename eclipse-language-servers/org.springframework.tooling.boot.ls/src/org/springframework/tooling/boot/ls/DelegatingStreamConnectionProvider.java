/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.springframework.ide.eclipse.boot.dash.remoteapps.RemoteBootAppsDataHolder;
import org.springframework.ide.eclipse.boot.dash.remoteapps.RemoteBootAppsDataHolder.RemoteAppData;
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
	private final ValueListener<ImmutableSet<RemoteAppData>> remoteAppsListener = (e, v) -> sendConfiguration();
	
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
		RemoteBootAppsDataHolder.getDefault().getRemoteApps().removeListener(remoteAppsListener);
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
				RemoteBootAppsDataHolder.getDefault().getRemoteApps().addListener(remoteAppsListener);
			}
		}
	}
	
	
	private void sendConfiguration() {
		Map<String, Object> settings = new HashMap<>();
		Map<String, Object> bootJavaObj = new HashMap<>();
		Map<String, Object> liveInformation = new HashMap<>();
		Map<String, Object> liveInformationAutomaticTracking = new HashMap<>();
		Map<String, Object> liveInformationFetchData = new HashMap<>();
		Map<String, Object> supportXML = new HashMap<>();
		Map<String, Object> bootChangeDetection = new HashMap<>();
		Map<String, Object> scanTestJavaSources = new HashMap<>();
		Map<String, Object> validation = new HashMap<>();
		Map<String, Object> validationSpelExpressions = new HashMap<>();

		IPreferenceStore preferenceStore = BootLanguageServerPlugin.getDefault().getPreferenceStore();

		liveInformationAutomaticTracking.put("on", preferenceStore.getBoolean(Constants.PREF_LIVE_INFORMATION_AUTOMATIC_TRACKING_ENABLED));
		liveInformationAutomaticTracking.put("delay", preferenceStore.getInt(Constants.PREF_LIVE_INFORMATION_AUTOMATIC_TRACKING_DELAY));
		
		liveInformationFetchData.put("max-retries", preferenceStore.getInt(Constants.PREF_LIVE_INFORMATION_FETCH_DATA_RETRY_MAX_NO));
		liveInformationFetchData.put("retry-delay-in-seconds", preferenceStore.getInt(Constants.PREF_LIVE_INFORMATION_FETCH_DATA_RETRY_DELAY_IN_SECONDS));
		
		liveInformation.put("automatic-tracking", liveInformationAutomaticTracking);
		liveInformation.put("fetch-data", liveInformationFetchData);
		
		supportXML.put("on", preferenceStore.getBoolean(Constants.PREF_SUPPORT_SPRING_XML_CONFIGS));
		supportXML.put("scan-folders", preferenceStore.getString(Constants.PREF_XML_CONFIGS_SCAN_FOLDERS));
		supportXML.put("hyperlinks", preferenceStore.getString(Constants.PREF_XML_CONFIGS_HYPERLINKS));
		supportXML.put("content-assist", preferenceStore.getString(Constants.PREF_XML_CONFIGS_CONTENT_ASSIST));
		bootChangeDetection.put("on", preferenceStore.getBoolean(Constants.PREF_CHANGE_DETECTION));
		scanTestJavaSources.put("on", preferenceStore.getBoolean(Constants.PREF_SCAN_JAVA_TEST_SOURCES));

		validationSpelExpressions.put("on", preferenceStore.getBoolean(Constants.PREF_VALIDATION_SPEL_EXPRESSIONS));
		validation.put("spel", validationSpelExpressions);

		bootJavaObj.put("live-information", liveInformation);
		bootJavaObj.put("support-spring-xml-config", supportXML);
		bootJavaObj.put("change-detection", bootChangeDetection);
		bootJavaObj.put("scan-java-test-sources", scanTestJavaSources);
		bootJavaObj.put("change-detection", bootChangeDetection);
		bootJavaObj.put("validation", validation);

		bootJavaObj.put("remote-apps", getAllRemoteApps());

		settings.put("boot-java", bootJavaObj);

		this.languageServer.getWorkspaceService().didChangeConfiguration(new DidChangeConfigurationParams(settings));
	}

	/**
	 * Combines remote boot app data from all configuration sources.
	 */
	protected Collection<RemoteAppData> getAllRemoteApps() {
		return RemoteBootAppsDataHolder.getDefault().getRemoteApps().getValues();
	}
	
}
