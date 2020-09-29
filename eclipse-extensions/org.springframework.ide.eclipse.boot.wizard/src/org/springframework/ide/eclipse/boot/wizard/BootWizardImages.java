/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * @author Terry Denney
 */
public class BootWizardImages {

	private static final String WIZBAN = "wizban";
	private static ImageRegistry imageRegistry;
	private static URL baseURL = null;

	public static final ImageDescriptor BOOT_SMALL_ICON= create("etool16", "boot-icon.png");
	public static final ImageDescriptor BOOT_WIZARD_ICON = create(WIZBAN, "boot_wizard.png");
	public static final ImageDescriptor GUIDES_WIZARD_ICON = create(WIZBAN, "guides_wizard.png");

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		}
		catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	public static Image getImage(ImageDescriptor imageDescriptor) {
		ImageRegistry imageRegistry = getImageRegistry();
		Image image = imageRegistry.get("" + imageDescriptor.hashCode());
		if (image == null) {
			image = imageDescriptor.createImage(true);
			imageRegistry.put("" + imageDescriptor.hashCode(), image);
		}
		return image;
	}

	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}
		return imageRegistry;
	}

	private static void initBaseURL() {
		if (baseURL == null) {
			baseURL = BootWizardActivator.getDefault().getBundle().getEntry("/icons/full/");
		}
	}

	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		initBaseURL();
		if (baseURL == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append("/");
		buffer.append(name);
		return new URL(baseURL, buffer.toString());
	}

}
