/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons;

import java.net.URL;
import java.time.Duration;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.tooling.ls.eclipse.commons.STS4LanguageClientImpl.UpdateHighlights;
import org.springframework.tooling.ls.eclipse.commons.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class LanguageServerCommonsActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.springframework.tooling.ls.eclipse.commons";

	public static final String BOOT_KEY = "boot-key";

	private static LanguageServerCommonsActivator instance;

	private ColorRegistry colorRegistry;

	private final IPropertyChangeListener PROPERTY_LISTENER = new IPropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			switch (event.getProperty()) {
			case PreferenceConstants.HIGHLIGHT_RANGE_COLOR_PREFS:
				RGB prefsColor = PreferenceConverter
						.getColor(EditorsPlugin.getDefault().getPreferenceStore(), PreferenceConstants.HIGHLIGHT_RANGE_COLOR_PREFS);
				// Convert color to without alpha with background
				RGB derivedColor = convertRGBtoNonTransparent(prefsColor);
				colorRegistry.put(PreferenceConstants.HIGHLIGHT_RANGE_COLOR_PREFS, derivedColor);
				// No break - need to update highlights for the new color to take effect
			case PreferenceConstants.HIGHLIGHT_CODELENS_PREFS:
				new UpdateHighlights(null);
				break;
			default:
			}
		}

	};

	private static int convertColorToNonTransparent(int color, int bg, double alpha) {
		int x = (int) Math.round((color - (1 - alpha) * bg) / alpha);
		x = Math.max(0, x);
		x = Math.min(x, 0xFF);
		return x;
	}

	private static RGB convertRGBtoNonTransparent(RGB rgb) {
		double alpha = 0.25;
		RGB bg = new RGB(0xFF, 0xFF, 0xFF); // white
		return new RGB(
				convertColorToNonTransparent(rgb.red, bg.red, alpha),
				convertColorToNonTransparent(rgb.green, bg.green, alpha),
				convertColorToNonTransparent(rgb.blue, bg.blue, alpha)
		);
	}

	public LanguageServerCommonsActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		instance = this;
		super.start(context);
		getImageRegistry().put(BOOT_KEY, getImageDescriptor("icons/boot.png"));

		UIJob uiJob = new UIJob("Setup color registry") {
			{
				setSystem(true);
			}

			@Override
			public IStatus runInUIThread(IProgressMonitor arg0) {
				colorRegistry = new ColorRegistry(PlatformUI.getWorkbench().getDisplay(), true);
				RGB prefsColor = PreferenceConverter.getColor(EditorsPlugin.getDefault().getPreferenceStore(), PreferenceConstants.HIGHLIGHT_RANGE_COLOR_PREFS);
				colorRegistry.put(PreferenceConstants.HIGHLIGHT_RANGE_COLOR_PREFS, convertRGBtoNonTransparent(prefsColor));
				getPreferenceStore().addPropertyChangeListener(PROPERTY_LISTENER);
				EditorsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(PROPERTY_LISTENER);
				return Status.OK_STATUS;
			}
		};
		uiJob.schedule();
	}

	public Color getBootHighlightRangeColor() {
		if (colorRegistry!=null) {
			return colorRegistry.get(PreferenceConstants.HIGHLIGHT_RANGE_COLOR_PREFS);
		}
		return null;
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
		EditorsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(PROPERTY_LISTENER);
		getPreferenceStore().removePropertyChangeListener(PROPERTY_LISTENER);
		super.stop(context);
	}

	public static LanguageServerCommonsActivator getInstance() {
		return instance;
	}

	public static void logError(Throwable t, String message) {
		instance.getLog().log(new Status(IStatus.ERROR, instance.getBundle().getSymbolicName(), message, t));
	}

	public static void logInfo(String message) {
		instance.getLog().log(new Status(IStatus.INFO, instance.getBundle().getSymbolicName(), message));
	}
}
