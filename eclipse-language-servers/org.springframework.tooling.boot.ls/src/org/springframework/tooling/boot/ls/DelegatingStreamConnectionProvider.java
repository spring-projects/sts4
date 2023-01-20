/*******************************************************************************
 * Copyright (c) 2017, 2023 Pivotal, Inc.
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
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.notifications.AbstractNotificationPopup;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.springframework.tooling.boot.ls.prefs.CategoryProblemsSeverityPrefsPage;
import org.springframework.tooling.boot.ls.prefs.FileListEditor;
import org.springframework.tooling.boot.ls.prefs.ProblemCategoryData;
import org.springframework.tooling.boot.ls.prefs.ProblemCategoryData.CategoryToggleData;
import org.springframework.tooling.boot.ls.prefs.StringListEditor;
import org.springsource.ide.eclipse.commons.boot.ls.remoteapps.RemoteBootAppsDataHolder;
import org.springsource.ide.eclipse.commons.boot.ls.remoteapps.RemoteBootAppsDataHolder.RemoteAppData;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

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
	
	private static final List<String> PREFS_EXCLUSIONS = List.of(Constants.PREF_START_LS_EARLY);
	private final IPropertyChangeListener configListener = (e) -> {
		if (!PREFS_EXCLUSIONS.contains(e.getProperty())) {
			sendConfiguration();
		}
	};
	
	private final ValueListener<ImmutableSet<RemoteAppData>> remoteAppsListener = (e, v) -> sendConfiguration();
	
	private long timestampBeforeStart;
	private long timestampWhenInitialized;
	
	public DelegatingStreamConnectionProvider() {
//		LanguageServerCommonsActivator.logInfo("Entering DelegatingStreamConnectionProvider()");
		String port = System.getProperty("boot-java-ls-port");
		if (port != null) {
			this.provider = new SpringBootLanguageServerViaSocket(Integer.parseInt(port));
		} else {
//			LanguageServerCommonsActivator.logInfo("DelegatingStreamConnectionProvider classloader = "+this.getClass().getClassLoader());
			Assert.isNotNull(SpringBootLanguageServer.class);
//			LanguageServerCommonsActivator.logInfo("SpringBootLanguageServer exists!");
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
//		BootLanguageServerPlugin.getPreferences().removePreferenceChangeListener(prefsListener);
		RemoteBootAppsDataHolder.getDefault().getRemoteApps().removeListener(remoteAppsListener);
	}

	@Override
	public void handleMessage(Message message, LanguageServer languageServer, URI rootURI) {
		if (message instanceof ResponseMessage) {
			ResponseMessage responseMessage = (ResponseMessage)message;
			if (responseMessage.getResult() instanceof InitializeResult) {
				this.languageServer = languageServer;
				
				this.timestampWhenInitialized = System.currentTimeMillis();
//				LanguageServerCommonsActivator.logInfo("Boot LS startup time from start to initialized: " + (timestampWhenInitialized - timestampBeforeStart) + "ms");

				sendConfiguration();
				
				// Add config listener
				BootLanguageServerPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(configListener);
//				BootLanguageServerPlugin.getPreferences().addPreferenceChangeListener(prefsListener);

				// Add resource listener
				ResourcesPlugin.getWorkspace().addResourceChangeListener(fResourceListener = new ResourceListener(languageServer, Arrays.asList(
						FileSystems.getDefault().getPathMatcher("glob:**/pom.xml"),
						FileSystems.getDefault().getPathMatcher("glob:**/*.xml"),
						FileSystems.getDefault().getPathMatcher("glob:**/*.gradle"),
						FileSystems.getDefault().getPathMatcher("glob:**/*.java"),
						FileSystems.getDefault().getPathMatcher("glob:**/*.json"),
						FileSystems.getDefault().getPathMatcher("glob:**/*.yml"),
						FileSystems.getDefault().getPathMatcher("glob:**/*.properties"),
						FileSystems.getDefault().getPathMatcher("glob:**/META-INF/spring/*.factories")
				)));
				
				//Add remote boot apps listener
				RemoteBootAppsDataHolder.getDefault().getRemoteApps().addListener(remoteAppsListener);
				
				IPreferenceStore preferenceStore = BootLanguageServerPlugin.getDefault().getPreferenceStore();
				
				if (preferenceStore.getBoolean(Constants.PREF_REWRITE_RECONCILE_PROMPT) && !preferenceStore.getBoolean(Constants.PREF_REWRITE_RECONCILE)) {
					PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
						try {
							NotificationQuestionWithLink question = new NotificationQuestionWithLink(
									"Spring Java Reconcile",
									"Do you wish to enable additional Java sources reconciling to get Spring specific validations and suggestions?\n\n" +
									"See <a>Validations And Quick Fixes</a> for more details.",
									new URL("https://github.com/spring-projects/sts4/wiki/Validations-And-Quick-Fixes"),
									() -> preferenceStore.setValue(Constants.PREF_REWRITE_RECONCILE, true),
									() -> {},
									() -> preferenceStore.setValue(Constants.PREF_REWRITE_RECONCILE_PROMPT, false));
							question.setDelayClose(-1);
							question.open();							
						} catch (Exception e) {
							BootLanguageServerPlugin.getDefault().getLog().error("Failed to Spring Java Reconcile popup", e);
						}
					});
				}
			}
		}
	}
	
	
	private void sendConfiguration() {
		Map<String, Object> settings = new HashMap<>();
		Map<String, Object> bootJavaObj = new HashMap<>();
		Map<String, Object> liveInformation = new HashMap<>();
		Map<String, Object> liveInformationFetchData = new HashMap<>();
		Map<String, Object> supportXML = new HashMap<>();
		Map<String, Object> bootChangeDetection = new HashMap<>();
		Map<String, Object> scanTestJavaSources = new HashMap<>();
		Map<String, Object> validation = new HashMap<>();
		Map<String, Object> validationSpelExpressions = new HashMap<>();

		IPreferenceStore preferenceStore = BootLanguageServerPlugin.getDefault().getPreferenceStore();

		liveInformationFetchData.put("max-retries", preferenceStore.getInt(Constants.PREF_LIVE_INFORMATION_FETCH_DATA_RETRY_MAX_NO));
		liveInformationFetchData.put("retry-delay-in-seconds", preferenceStore.getInt(Constants.PREF_LIVE_INFORMATION_FETCH_DATA_RETRY_DELAY_IN_SECONDS));
		
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
		
		bootJavaObj.put("rewrite", Map.of(
				"reconcile", preferenceStore.getBoolean(Constants.PREF_REWRITE_RECONCILE),
				"recipe-filters", StringListEditor.decode(preferenceStore.getString(Constants.PREF_REWRITE_RECIPE_FILTERS)),
				"scan-directories", FileListEditor.getValuesFromPreference(preferenceStore.getString(Constants.PREF_REWRITE_RECIPES_SCAN_DIRS)),
				"scan-files", FileListEditor.getValuesFromPreference(preferenceStore.getString(Constants.PREF_REWRITE_RECIPES_SCAN_FILES))
		));
		settings.put("boot-java", bootJavaObj);
		
		putValidationPreferences(settings);
		putValidationCategoryToggles(settings);

		this.languageServer.getWorkspaceService().didChangeConfiguration(new DidChangeConfigurationParams(settings));
	}
	
	private void putValidationPreferences(Map<String, Object> settings) {
		try {
			IEclipsePreferences prefs = BootLanguageServerPlugin.getPreferences();
			for (String key : prefs.keys()) {
				if (key.startsWith("problem.")) {
					String val = prefs.get(key, null);
					if (val!=null) {
						dotPut(settings, "spring-boot.ls."+key, val);
					}
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
	}
	
	private void putValidationCategoryToggles(Map<String, Object> settings) {
		try {
			IEclipsePreferences prefs = BootLanguageServerPlugin.getPreferences();
			for (ProblemCategoryData category : CategoryProblemsSeverityPrefsPage.ALL_PROBLEM_CATEGORIES) {
				if (category.getToggle() != null) {
					CategoryToggleData toggle = category.getToggle();
					String val = prefs.get(toggle.getPreferenceKey(), null);
					if (val != null) {
						dotPut(settings, toggle.getPreferenceKey(), val);
					}
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
	}

	private void dotPut(Object _settings, String dottedProperty, Object value) {
		if (_settings instanceof Map) {
			Map<String, Object> settings = (Map<String, Object>) _settings;
			int dot = dottedProperty.indexOf('.');
			if (dot>=0) {
				String first = dottedProperty.substring(0, dot);
				String rest = dottedProperty.substring(dot+1);
				Object nested = settings.getOrDefault(first, null);
				if (nested==null) {
					nested = new HashMap<>();
					settings.put(first, nested);
				}
				dotPut(nested, rest, value);
			} else {
				settings.put(dottedProperty, value);
			}
		}
	}

	/**
	 * Combines remote boot app data from all configuration sources.
	 */
	protected Collection<RemoteAppData> getAllRemoteApps() {
		return RemoteBootAppsDataHolder.getDefault().getRemoteApps().getValues();
	}
	
	private static class NotificationQuestionWithLink extends AbstractNotificationPopup {

		private final String title;
		private final String textWithLinkTag;
		private final URL linkUrl;
		
		/*
		 * Notification open does not block on open and ignores setBlockOnOpen(true)
		 * Therefore handlers are passed directly in until this changes
		 */
		private Runnable yesHandler;
		private Runnable noHandler;
		private Runnable stopAskingHandler;
		

		public NotificationQuestionWithLink(String title, String textWithLinkTag, URL linkUrl,
				Runnable yesHandler, Runnable noHandler, Runnable stopAskingHandler) {
			super(Display.getCurrent());
			
			this.title = title;
			this.textWithLinkTag = textWithLinkTag;
			this.linkUrl = linkUrl;
			this.yesHandler = yesHandler;
			this.noHandler = noHandler;
			this.stopAskingHandler = stopAskingHandler;
			
			setParentShell(PlatformUI.getWorkbench().getDisplay().getActiveShell());
			setBlockOnOpen(true);
		}

		@Override
		public String getPopupShellTitle() {
			return title;
		}

		@Override
		protected void createContentArea(Composite parent) {
			Link link = new Link(parent, SWT.WRAP);
			link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			link.setText(textWithLinkTag);	
			if (linkUrl != null) {
				link.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(linkUrl);
						} catch (PartInitException ex) {
							BootLanguageServerPlugin.getDefault().getLog().error("Failed to open browser", ex);
						}
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
			}
			
			Composite buttonsComposite = new Composite(parent, SWT.None);
			buttonsComposite.setLayoutData(GridDataFactory.swtDefaults().grab(true, false).align(SWT.RIGHT, SWT.CENTER).create());
			GridLayout buttonsLayout = new GridLayout(3, false);
			buttonsLayout.horizontalSpacing = 0;
			buttonsComposite.setLayout(buttonsLayout);
			
			Button yesButton = new Button(buttonsComposite, SWT.None);
			yesButton.setText("Yes");
			yesButton.setLayoutData(GridDataFactory.swtDefaults().create());
			yesButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					super.widgetSelected(e);
					setReturnCode(SWT.YES);
					close();
					yesHandler.run();
				}
			});
			
			Button noButton = new Button(buttonsComposite, SWT.None);
			noButton.setText("No");
			noButton.setLayoutData(GridDataFactory.swtDefaults().create());
			noButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					super.widgetSelected(e);
					setReturnCode(SWT.NO);
					close();
					noHandler.run();
				}
			});
			
			Button stopAskingButton = new Button(buttonsComposite, SWT.None);
			stopAskingButton.setText("Stop Asking");
			stopAskingButton.setLayoutData(GridDataFactory.swtDefaults().create());
			stopAskingButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					super.widgetSelected(e);
					setReturnCode(SWT.SAVE);
					close();
					stopAskingHandler.run();
				}
			});
			
			parent.setBackground(link.getBackground());
		}

		@Override
		public Image getPopupShellImage(int maximumHeight) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
		}

	}

}
