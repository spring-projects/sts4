/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.tooling.ls.eclipse.commons.STS4LanguageClientImpl.UpdateHighlights;
import org.springframework.tooling.ls.eclipse.commons.preferences.PreferenceConstants;

public class LanguageServerCommonsActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.springframework.tooling.ls.eclipse.commons";

	public static final String BOOT_KEY = "boot-key";

	private static LanguageServerCommonsActivator instance;

	private static final IPropertyChangeListener PROPERTY_LISTENER = new IPropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (PreferenceConstants.HIGHLIGHT_CODELENS_PREFS.equals(event.getProperty())) {
				new UpdateHighlights(null);
			}
		}

	};

	public LanguageServerCommonsActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		instance = this;
		super.start(context);
		getImageRegistry().put(BOOT_KEY, getImageDescriptor("icons/boot.png"));
		getPreferenceStore().addPropertyChangeListener(PROPERTY_LISTENER);
	}

	public final static ImageDescriptor getImageDescriptor(String path) {
		ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
		Bundle bundle = Platform.getBundle(PLUGIN_ID);
		URL url = null;
		if (bundle != null) {
			url = FileLocator.find(bundle, new Path(path), null);
			if (url != null) {
				desc = ImageDescriptor.createFromURL(url);
			}
		}
		return desc;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		getPreferenceStore().removePropertyChangeListener(PROPERTY_LISTENER);
		super.stop(context);
	}

	public static LanguageServerCommonsActivator getInstance() {
		return instance;
	}

	public static void logError(Throwable t, String message) {
		instance.getLog().log(new Status(IStatus.ERROR, instance.getBundle().getSymbolicName(), message, t));
	}
}
