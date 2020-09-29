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
package org.springframework.ide.eclipse.boot.dash.cf.actions;

import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.liveprocess.LiveProcessCommandsExecutor;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashAction;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springframework.ide.eclipse.boot.dash.views.ToggleBootDashModelConnection;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashElementsAction.Params;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

import com.google.common.collect.ImmutableList;

public class CfBootDashActions {

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

		ImmutableList.Builder<AbstractBootDashAction> builder = ImmutableList.builder();
		builder.add(new RestartApplicationOnlyAction(defaultActionParams));
		builder.add(new SelectManifestAction(defaultActionParams));
		builder.add(new CloudFoundryRestartWithRemoteDevClientAction(defaultActionParams));
		builder.add(new EnableJmxSshTunnelAction(defaultActionParams));
		builder.add(new ReconnectCloudConsoleAction(defaultActionParams));
		if (section!=null) {
			builder.add(new UpdatePasswordAction(section, context));
			builder.add(new OpenCloudAdminConsoleAction(section, context));
			builder.add(new CustmomizeTargetAppManagerURLAction(section, context));
		}
		return builder.build();
	};
}
