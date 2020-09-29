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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.icons;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.springsource.ide.eclipse.commons.frameworks.ui.FrameworkUIActivator;


/**
 * @author Nieraj Singh
 */
public class IconManager {
	private Map<IIcon, ImageDescriptor> icons = new HashMap<IIcon, ImageDescriptor>();

	public Image getIcon(IIcon key) {
		if (key == null) {
			return null;
		}

		ImageDescriptor descriptor = icons.get(key);
		if (descriptor == null) {
			try {
				String location = key.getIconLocation();
				URL url = new URL(location);
				if (location != null) {
					descriptor = ImageDescriptor.createFromURL(url);
					icons.put(key, descriptor);
				}
			} catch (MalformedURLException e) {
				FrameworkUIActivator.log(new Status(IStatus.ERROR, FrameworkUIActivator.PLUGIN_ID, "Invalid URL: " + key.getIconLocation()));
			}
		}
		return descriptor != null ? descriptor.createImage() : null;
	}
	
}
