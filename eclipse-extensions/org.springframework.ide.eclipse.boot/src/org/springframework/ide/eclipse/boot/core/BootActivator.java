/*******************************************************************************
 * Copyright (c) 2013, 2020 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

/**
 * The activator class controls the plug-in life cycle
 */
public class BootActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.boot"; //$NON-NLS-1$

	// The shared instance
	private static BootActivator plugin;

	/**
	 * The constructor
	 */
	public BootActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		JDK9CompatibilityCheck.initialize();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static BootActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns a new <code>IStatus</code> for this plug-in
	 */
	public static IStatus createErrorStatus(Throwable exception) {
		String message = exception.getMessage();
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

	public static IStatus createInfoStatus(String msg) {
		return new Status(IStatus.INFO, PLUGIN_ID, msg);
	}

	/**
	 * Deprecated use {@link Log}.log() instead.
	 */
	@Deprecated public static void log(Throwable e) {
		Log.log(e);
	}

	/**
	 * Deprecated use {@link Log}.info() instead.
	 */
	@Deprecated public static void info(String msg) {
		Log.info(msg);
	}

	public static URLConnectionFactory getUrlConnectionFactory() {
		return getUrlConnectionFactory(null);
	}

	public static URLConnectionFactory getUrlConnectionFactory(Consumer<URLConnection> customizer) {
		final String userAgent = "STS/"+getDefault().getBundle().getVersion();
		return new URLConnectionFactory() {
			@Override
			public URLConnection createConnection(URL url) throws IOException {
				URLConnection conn = super.createConnection(url);
				conn.addRequestProperty("User-Agent", userAgent);
				if (customizer != null) {
					customizer.accept(conn);
				}
				return conn;
			}
		};
	}
}
