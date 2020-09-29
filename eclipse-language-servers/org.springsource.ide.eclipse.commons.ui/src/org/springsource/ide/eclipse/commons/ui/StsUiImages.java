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
package org.springsource.ide.eclipse.commons.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.springsource.ide.eclipse.commons.internal.ui.UiPlugin;

/**
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Terry Denney
 * @author Christian Dupuis
 */
public class StsUiImages {

	private static ImageRegistry imageRegistry;

	private static final String T_VIEW = "view16";

	private static final String WIZBAN = "wizban";

	private static final String OBJ = "obj16";

	private static final URL baseURL = UiPlugin.getDefault().getBundle().getEntry("/icons/");

	// com.springsource.sts.runtimeerroranalysis

	public static final ImageDescriptor RUNTIME_ERROR = create(T_VIEW, "analysis-log.png");

	public static final ImageDescriptor RUNTIME_ERROR_ANALYSIS_SPRINGSOURCE = create(T_VIEW,
			"analysis-springsource.png");

	public static final ImageDescriptor RUNTIME_ERROR_ANALYSIS_COMMUNITY = create(T_VIEW, "analysis-community.png");

	public static final ImageDescriptor ERROR = create(T_VIEW, "error.png");

	public static final ImageDescriptor INFO = create(T_VIEW, "info.png");

	// com.springsource.sts.config.ui

	public static final ImageDescriptor XML_FILE = create(T_VIEW, "xmlfile.gif");

	public static final ImageDescriptor NAMESPACE_CONFIG_ICON = create(WIZBAN, "namespace.png");

	// com.springsource.sts.wizard

	public static final ImageDescriptor DOWNLOAD_OVERLAY = create(OBJ, "download_overlay.png");

	// com.springsource.sts.ide.ui

	public static final ImageDescriptor WARNING = create(OBJ, "warning.png");

	public static final ImageDescriptor IMPORTANT = create(OBJ, "priority-1.png");

	public static final ImageDescriptor RSS_CONFIGURE = create(OBJ, "rss_configure.png");

	public static final ImageDescriptor RSS = create(OBJ, "rss.png");

	public static final ImageDescriptor NEW_JAVA_PROJECT = create(OBJ, "new_java_project.gif");

	// com.springsource.sts.ide.help

	public static final ImageDescriptor CHEATSHEET = create(OBJ, "tutorials.png");

	// tip o' the day icons

	public static final ImageDescriptor TIP_CLOSE = create(OBJ, "close.gif");

	public static final ImageDescriptor TIP_CLOSE_HOT = create(OBJ, "close_hot.gif");

	public static final ImageDescriptor TIP_NEXT = create(OBJ, "next.gif");

	public static final ImageDescriptor TIP_PREV = create(OBJ, "prev.gif");

	// unused

	public static final ImageDescriptor TEMPLATE_PROJECT = create(WIZBAN, "template_project.png");

	public static final ImageDescriptor PROJECT = create(WIZBAN, "project.png");

	public static final ImageDescriptor JAVA_PROJECT = create(WIZBAN, "java_project.png");

	public static final ImageDescriptor GRAILS_PROJECT = create(WIZBAN, "grails_project.png");

	public static final ImageDescriptor NEW_GRAILS_PROJECT = create(OBJ, "new_grails_project.png");

	public static final ImageDescriptor NEW_ROO_PROJECT = create(OBJ, "new_roo_project.png");

	public static final ImageDescriptor NEW_SPRING_PROJECT = create(OBJ, "new_spring_project.gif");

	public static final ImageDescriptor NEW_TEMPLATE_PROJECT = create(OBJ, "new_template_project.png");

	public static final ImageDescriptor NEW_AJDT_PROJECT = create(OBJ, "new_ajdt_project.gif");

	public static final ImageDescriptor SPRING_LOGO = create(T_VIEW, "spring.png");

	public static final ImageDescriptor SPRING_LOGO_NOTIFY = create(T_VIEW, "spring_notify.png");

	/**
	 * Lazily initializes image map.
	 */
	public static Image getImage(ImageDescriptor imageDescriptor) {
		ImageRegistry imageRegistry = getImageRegistry();
		Image image = imageRegistry.get("" + imageDescriptor.hashCode());
		if (image == null) {
			image = imageDescriptor.createImage(true);
			imageRegistry.put("" + imageDescriptor.hashCode(), image);
		}
		return image;
	}

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		}
		catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}
		return imageRegistry;
	}

	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		if (baseURL == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(baseURL, buffer.toString());
	}

}
