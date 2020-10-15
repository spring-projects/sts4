/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.xterm;

import org.springframework.ide.eclipse.boot.dash.di.EclipseBeanLoader.Contribution;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;

public class BootDashInjections implements Contribution {

	@Override
	public void applyBeanDefinitions(SimpleDIContext context) throws Exception {
		context.defInstance(BootDashActions.Factory.class, DockerBootDashActions.factory);
	}
}
