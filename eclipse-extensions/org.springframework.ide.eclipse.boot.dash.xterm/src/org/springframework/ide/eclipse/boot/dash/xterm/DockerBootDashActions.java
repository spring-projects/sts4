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

import java.util.Arrays;

import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.liveprocess.LiveProcessCommandsExecutor;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashElementsAction.Params;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

public class DockerBootDashActions {

	public static BootDashActions.Factory factory = (
			BootDashActions actions,
			BootDashViewModel model,
			MultiSelection<BootDashElement> selection,
			LiveExpression<BootDashModel> section,
			SimpleDIContext context,
			LiveProcessCommandsExecutor liveProcessCmds
	) -> {

		Params defaultActionParams = new Params(actions)
				.setModel(model)
				.setSelection(selection)
				.setContext(context)
				.setLiveProcessCmds(liveProcessCmds);
		return Arrays.asList(
				new OpenDockerTerminal(defaultActionParams)
		);
	};
}
