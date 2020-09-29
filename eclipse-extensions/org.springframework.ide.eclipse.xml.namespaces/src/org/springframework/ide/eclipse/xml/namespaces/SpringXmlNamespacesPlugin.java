/*******************************************************************************
 * Copyright (c) 2004, 2015 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces;

import java.util.Hashtable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.springframework.ide.eclipse.xml.namespaces.internal.ProjectClasspathNamespaceDefinitionResolverCache;
import org.springframework.ide.eclipse.xml.namespaces.model.INamespaceDefinitionResolver;
import org.springframework.ide.eclipse.xml.namespaces.ui.XmlNamespacesUIImages;

/**
 * Central access point for the Spring Framework Core plug-in (id
 * <code>"org.springframework.ide.eclipse.beans.core"</code>).
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Tomasz Zarna
 * @author Martin Lippert
 */
public class SpringXmlNamespacesPlugin extends AbstractUIPlugin {
	
	/**
	 * Plugin identifier for Spring Beans Core (value <code>org.springframework.ide.eclipse.beans.core</code>).
	 */
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.xml.namespaces";

	/** preference key to load namespace handler by searching source folders */
	public static final String DISABLE_CACHING_FOR_NAMESPACE_LOADING_ID = "disableCachingForNamespaceLoadingFromClasspath";

	/** preference key to load namespace handler from classpath */
	public static final String LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID = "loadNamespaceHandlerFromClasspath";

	/** preference key to specify the default namespace version */
	public static final String NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID = "default.version.";

	/** preference key to specify if versions should be taken from the classpath */
	public static final String NAMESPACE_DEFAULT_FROM_CLASSPATH_ID = "default.version.check.classpath";

	/** preference key to specify the default namespace version */
	public static final String NAMESPACE_PREFIX_PREFERENCE_ID = "prefix.";

	/** preference key to specify whether wizards automatically convert all newly inserted namespace 
	 *  locations into https */
	public static final String USE_HTTPS_FOR_SCHEMA_LOCATIONS = "use.https.for.new.namespace.locations";

	/** preference key to enable namespace versions per namespace */
	public static final String PROJECT_PROPERTY_ID = "enable.project.preferences";

	/** preference key to suppress missing namespace handler warnings */
	public static final String IGNORE_MISSING_NAMESPACEHANDLER_PROPERTY = "ignoreMissingNamespaceHandler";

	public static final boolean IGNORE_MISSING_NAMESPACEHANDLER_PROPERTY_DEFAULT = false;


	/** The shared instance */
	private static SpringXmlNamespacesPlugin plugin;
	
	/**
	 * Creates the Spring Beans Core plug-in.
	 * <p>
	 * The plug-in instance is created automatically by the Eclipse platform. Clients must not call.
	 */
	public SpringXmlNamespacesPlugin() {
		plugin = this;
	}

	/**
	 * Monitor used for dealing with the bundle activator and synchronous bundle threads
	 */
	private transient final Object monitor = new Object();

	/**
	 * flag indicating whether the context is down or not - useful during shutdown
	 */
	private volatile boolean isClosed = false;

	private ServiceRegistration<?> projectAwareUrlService = null;

	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		XmlNamespacesUIImages.initializeImageRegistry(registry);
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);

		Hashtable<String, String> properties = new Hashtable<String, String>();
		properties.put(URLConstants.URL_HANDLER_PROTOCOL,
				ProjectAwareUrlStreamHandlerService.PROJECT_AWARE_PROTOCOL);
		projectAwareUrlService = context.registerService(
				URLStreamHandlerService.class.getName(),
				new ProjectAwareUrlStreamHandlerService(), properties);
		
//		executorService = Executors.newCachedThreadPool(new ThreadFactory() {
//			
//			public Thread newThread(Runnable runnable) {
//				Version version = Version.parseVersion(getPluginVersion());
//				String productId = "Spring IDE";
//				IProduct product = Platform.getProduct();
//				if (product != null && "com.springsource.sts".equals(product.getId()))
//						productId = "STS";
//				Thread reportingThread = new Thread(runnable, String.format(THREAD_NAME_TEMPLATE, threadCount.incrementAndGet(), 
//						productId, version.getMajor(), version.getMinor(), version.getMicro()));
//				reportingThread.setDaemon(true);
//				return reportingThread;
//			}
//		});

		
//		getPreferenceStore().setDefault(TIMEOUT_CONFIG_LOADING_PREFERENCE_ID, 60);
		getPreferenceStore().setDefault(NAMESPACE_DEFAULT_FROM_CLASSPATH_ID, true);
		getPreferenceStore().setDefault(LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID, true);

	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		synchronized (monitor) {
			// if already closed, bail out
			if (isClosed) {
				return;
			}
			isClosed = true;
		}
//		model.stop();
		if (projectAwareUrlService != null) {
			projectAwareUrlService.unregister();
		}
		super.stop(context);
	}

	public static boolean isDebug(String option) {
		String value = Platform.getDebugOption(option);
		return (value != null && value.equalsIgnoreCase("true") ? true : false);
	}

	/**
	 * Returns the shared instance.
	 */
	public static SpringXmlNamespacesPlugin getDefault() {
		return plugin;
	}

	/**
	 * Writes the message to the plug-in's log
	 * 
	 * @param message the text to write to the log
	 */
	public static void log(String message, Throwable exception) {
		IStatus status = createErrorStatus(message, exception);
		getDefault().getLog().log(status);
	}

	public static void log(Throwable exception) {
		getDefault().getLog().log(createErrorStatus("Plugin.internal_error", exception));
	}

	public static IStatus createErrorStatus(String message, Throwable exception) {
		return createStatus(IStatus.ERROR, message, exception);
	}

	public static IStatus createStatus(int severity, String message, Throwable exception) {
		return new Status(severity, PLUGIN_ID, 0, message == null ? "" : message, exception);
	}

	public static void logAsWarning(Throwable exception) {
		getDefault().getLog().log(createWarningStatus("Plugin.internal_warning", exception));
	}

	public static IStatus createWarningStatus(String message, Throwable exception) {
		return createStatus(IStatus.WARNING, message, exception);
	}

	public static INamespaceDefinitionResolver getNamespaceDefinitionResolver(IProject project) {
		if (project != null) {
			return ProjectClasspathNamespaceDefinitionResolverCache.getResolver(project);
		}
		return getNamespaceDefinitionResolver();
	}

	public static INamespaceDefinitionResolver getNamespaceDefinitionResolver() {
		return NamespaceManagerProvider.get().getNamespaceDefinitionResolver();
	}

}
