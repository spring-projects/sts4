/*******************************************************************************
 * Copyright (c) 2016, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Phil Webb - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf;

import java.util.concurrent.CompletableFuture;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class BootDashCfPlugin extends AbstractUIPlugin {

	private static final String PLUGIN_ID = "org.springframework.ide.eclipse.boot.dash.cf";

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		CompletableFuture.runAsync(() -> BootDashTargetInfoSynchronizer.start());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		BootDashTargetInfoSynchronizer.stop();
		super.stop(context);
	}

}
