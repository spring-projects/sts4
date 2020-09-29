/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Live Beans plugin
 * 
 * @author Alex Boyko
 *
 */
public class LiveBeansUiPlugin extends AbstractUIPlugin {
	
	// The plug-in ID
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.beans.ui.live"; //$NON-NLS-1$
	
	private static final String ICON_PATH_PREFIX = "icons/";
	private static final String NAME_PREFIX = PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();
	
	public static final String IMG_OBJS_BEAN = NAME_PREFIX + "bean_obj.gif";
	public static final String IMG_OBJS_BEAN_REF = NAME_PREFIX + "beanref_obj.gif";
	public static final String IMG_OBJS_CONFIG = NAME_PREFIX + "config_obj.gif";
	public static final String IMG_OBJS_COLLECTION = NAME_PREFIX + "collection_obj.gif";

	private static LiveBeansUiPlugin plugin;
	

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static LiveBeansUiPlugin getDefault() {
		return plugin;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		reg.put(IMG_OBJS_BEAN, imageDescriptorFromPlugin(PLUGIN_ID, ICON_PATH_PREFIX + IMG_OBJS_BEAN.substring(NAME_PREFIX_LENGTH)));
		reg.put(IMG_OBJS_BEAN_REF, imageDescriptorFromPlugin(PLUGIN_ID, ICON_PATH_PREFIX + IMG_OBJS_BEAN_REF.substring(NAME_PREFIX_LENGTH)));
		reg.put(IMG_OBJS_CONFIG, imageDescriptorFromPlugin(PLUGIN_ID, ICON_PATH_PREFIX + IMG_OBJS_CONFIG.substring(NAME_PREFIX_LENGTH)));
		reg.put(IMG_OBJS_COLLECTION, imageDescriptorFromPlugin(PLUGIN_ID, ICON_PATH_PREFIX + IMG_OBJS_COLLECTION.substring(NAME_PREFIX_LENGTH)));
	}


}
