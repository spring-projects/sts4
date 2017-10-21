/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.java.ls;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Boot-Java LS extension plugin
 * 
 * @author Alex Boyko
 *
 */
public class BootJavaLanguageServerPlugin extends AbstractUIPlugin {
	
	public static final String ID = "org.springframework.tooling.boot.java.ls";
	
	// The shared instance
	private static BootJavaLanguageServerPlugin plugin;

	public BootJavaLanguageServerPlugin() {
		// Empty
	}

	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}
	
	public static BootJavaLanguageServerPlugin getDefault() {
		return plugin;
	}

}
