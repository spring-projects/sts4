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
package org.springframework.tooling.ls.eclipse.gotosymbol;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class GotoSymbolPlugin extends AbstractUIPlugin {

	public static final String ID = "org.springframework.tooling.ls.eclipse.gotosymbol";
	
	private static GotoSymbolPlugin instance;

	public GotoSymbolPlugin() {
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
	}

	public static GotoSymbolPlugin getInstance() {
		return instance;
	}

}
