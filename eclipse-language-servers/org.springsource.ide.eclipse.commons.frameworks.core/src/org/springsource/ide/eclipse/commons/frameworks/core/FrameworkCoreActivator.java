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
package org.springsource.ide.eclipse.commons.frameworks.core;

import java.net.URISyntaxException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springsource.ide.eclipse.commons.frameworks.core.legacyconversion.IConversionConstants;

/**
 * @author Nieraj Singh
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class FrameworkCoreActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.springsource.ide.eclipse.commons.frameworks.core"; //$NON-NLS-1$

	// The shared instance
	private static FrameworkCoreActivator plugin;

	/**
	 * The constructor
	 */
	public FrameworkCoreActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static FrameworkCoreActivator getDefault() {
		return plugin;
	}

	public static void logError(String message, Throwable exception) {
		getDefault().getLog().log(createErrorStatus(message, exception));
	}
	public static void log(Throwable e) {
		getDefault().getLog().log(createErrorStatus(e.getMessage(), e));
	}

	public static IStatus createErrorStatus(String message, Throwable exception) {
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

	public static IStatus createWarningStatus(String message,
			Throwable exception) {
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, exception);
	}
	
    /**
     * Returns true if the plugin id has not yet run legacy conversion for this workspace yet
     * @param pluginid plugin id to check
     * @return true iff legacy conversion has not taken place for this plugin, but it has for the workspace as a whole
     */
    public boolean shouldMigratePlugin(String pluginid) {
        if (getPreferenceStore().getBoolean(IConversionConstants.LEGACY_MIGRATION_ALREADY_DONE)) {
            String plugins = getPreferenceStore().getString(IConversionConstants.LEGACY_MIGRATION_PLUGINS);
            return plugins.contains("," + pluginid + ","); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            // workspace legacy migration has not yet been successfully performed
            return false;
        }
    }
    
    public void registerPluginMigrationComplete(String pluginid) {
        String plugins = getPreferenceStore().getString(IConversionConstants.LEGACY_MIGRATION_PLUGINS);
        plugins += "," + pluginid + ","; //$NON-NLS-1$ //$NON-NLS-2$
        getPreferenceStore().putValue(IConversionConstants.LEGACY_MIGRATION_PLUGINS, plugins);
    }

}
