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
package org.springframework.ide.eclipse.boot.dash.docker;

import org.springframework.ide.eclipse.boot.dash.di.EclipseBeanLoader.Contribution;
import org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerRunTargetType;
import org.springframework.ide.eclipse.boot.dash.docker.ui.DefaultDockerUserInteractions;
import org.springframework.ide.eclipse.boot.dash.docker.ui.DockerUserInteractions;
import org.springframework.ide.eclipse.boot.dash.docker.ui.ExtraDockerActions;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;

public class DockerBootDashInjections implements Contribution {

	@Override
	public void applyBeanDefinitions(SimpleDIContext c) throws Exception {
		c.def(DockerRunTargetType.class, DockerRunTargetType::new);
		c.def(DockerUserInteractions.class, DefaultDockerUserInteractions::new);
		c.defInstance(ExtraDockerActions.class, new ExtraDockerActions());
	}

}
