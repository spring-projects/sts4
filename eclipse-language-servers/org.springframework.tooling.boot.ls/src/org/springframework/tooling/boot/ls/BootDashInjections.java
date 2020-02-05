/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls;

import org.springframework.ide.eclipse.boot.dash.di.EclipseBeanLoader;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.DefaultBootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.remoteapps.RemoteBootAppsDataHolder;
import org.springframework.tooling.boot.ls.prefs.RemoteAppsFromPrefsDataContributor;

/**
 * Contributes bean definitions to {@link DefaultBootDashModelContext}
 */
public class BootDashInjections implements EclipseBeanLoader.Contribution {

	@Override
	public void applyBeanDefinitions(SimpleDIContext context) throws Exception {
		context.defInstance(RemoteBootAppsDataHolder.Contributor.class, RemoteAppsFromPrefsDataContributor.INSTANCE);
	}
}
