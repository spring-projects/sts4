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

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class LanguageServerCommonsActivator extends AbstractUIPlugin {

	private static LanguageServerCommonsActivator instance;

	public LanguageServerCommonsActivator() {
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		instance = this; 
		super.start(context);
	}

	public static LanguageServerCommonsActivator getInstance() {
		return instance;
	}


}
