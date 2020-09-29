/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kris De Volder - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.azure;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class BootDashAzurePlugin extends AbstractUIPlugin {

	private static final String PLUGIN_ID = "org.springframework.ide.eclipse.boot.dash.azure";

	private static final String AZURE_ICON = "azure-icon";

//	@Override
//	protected void initializeImageRegistry(ImageRegistry reg) {
//		super.initializeImageRegistry(reg);
//		reg.put(AZURE_ICON, getImageDescriptor("/icons/azure.png"));
//	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("BootDashAzurePlugin starting...");
		super.start(context);
	}

}
