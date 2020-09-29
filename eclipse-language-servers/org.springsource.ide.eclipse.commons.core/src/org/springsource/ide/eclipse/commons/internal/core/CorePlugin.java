/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.internal.core;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.Preferences;

/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class CorePlugin extends Plugin {

	public static final String PLUGIN_ID = "org.springsource.ide.eclipse.commons.core";

	private static CorePlugin plugin;

	private static final String UUID_PROPERTY_KEY = "install.id";

	public static IStatus createErrorStatus(String message, Throwable exception) {
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

	public static CorePlugin getDefault() {
		return plugin;
	}

	public synchronized static String getUUID() {
		ServiceReference ref = getDefault().getBundle().getBundleContext()
				.getServiceReference(IPreferencesService.class.getName());
		IPreferencesService prefService = (IPreferencesService) getDefault().getBundle().getBundleContext()
				.getService(ref);
		try {
			IEclipsePreferences prefs = (IEclipsePreferences) prefService.getRootNode().node(InstanceScope.SCOPE);

			Preferences p = prefs.node(PLUGIN_ID);
			if (StringUtils.isEmpty(p.get(UUID_PROPERTY_KEY, ""))) {
				p.put(UUID_PROPERTY_KEY, UUID.randomUUID().toString());
			}
			return p.get(UUID_PROPERTY_KEY, "");
		}
		finally {
			if (prefService != null) {
				getDefault().getBundle().getBundleContext().ungetService(ref);
			}
		}
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(String message, Throwable exception) {
		IStatus status = createErrorStatus(message, exception);
		log(status);
	}

	public static void log(Throwable exception) {
		log(createErrorStatus("Internal Error", exception));
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		savePluginPreferences();
	}

	public static void warn(String msg) {
		log(new Status(IStatus.WARNING, PLUGIN_ID, msg));
	}
}
