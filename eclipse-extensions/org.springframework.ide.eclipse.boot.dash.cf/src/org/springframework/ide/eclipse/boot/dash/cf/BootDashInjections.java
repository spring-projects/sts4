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
package org.springframework.ide.eclipse.boot.dash.cf;

import org.springframework.ide.eclipse.boot.dash.api.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.cf.actions.CfBootDashActions;
import org.springframework.ide.eclipse.boot.dash.cf.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.DefaultCloudFoundryClientFactoryV2;
import org.springframework.ide.eclipse.boot.dash.cf.debug.DebugSupport;
import org.springframework.ide.eclipse.boot.dash.cf.debug.SshDebugSupport;
import org.springframework.ide.eclipse.boot.dash.cf.debug.SshTunnelFactory;
import org.springframework.ide.eclipse.boot.dash.cf.debug.SshTunnelImpl;
import org.springframework.ide.eclipse.boot.dash.cf.jmxtunnel.CloudFoundryRemoteBootAppsDataContributor;
import org.springframework.ide.eclipse.boot.dash.cf.jmxtunnel.JmxSshTunnelManager;
import org.springframework.ide.eclipse.boot.dash.cf.labels.BootDashCfLabels;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTargetType;
import org.springframework.ide.eclipse.boot.dash.cf.ui.CfUserInteractions;
import org.springframework.ide.eclipse.boot.dash.cf.ui.DefaultCfUserInteractions;
import org.springframework.ide.eclipse.boot.dash.di.EclipseBeanLoader.Contribution;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.labels.BootDashLabels;
import org.springframework.ide.eclipse.boot.dash.model.DefaultBootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springsource.ide.eclipse.commons.boot.ls.remoteapps.RemoteBootAppsDataHolder;

/**
 * Contributes bean definitions to {@link DefaultBootDashModelContext}
 */
public class BootDashInjections implements Contribution {

	@Override
	public void applyBeanDefinitions(SimpleDIContext context) throws Exception {
		//TargetType
		context.def(RunTargetType.class, CloudFoundryRunTargetType::new);

		//Auto sync of cf deployed apps with JMX over SSH tunnel with boot ls (for live hover/data features).
		context.def(RemoteBootAppsDataHolder.Contributor.class, CloudFoundryRemoteBootAppsDataContributor::new);

		//UI actions
		context.defInstance(BootDashActions.Factory.class, CfBootDashActions.factory);

		//BootDashLabels
		context.defInstance(BootDashLabels.Contribution.class, BootDashCfLabels.jmxDecoration);

		//cf internal
		context.def(CfUserInteractions.class, DefaultCfUserInteractions::new);
		context.defInstance(DebugSupport.class, SshDebugSupport.INSTANCE);
		context.defInstance(SshTunnelFactory.class, SshTunnelImpl::new);
		context.defInstance(JmxSshTunnelManager.class, new JmxSshTunnelManager());
		context.defInstance(CloudFoundryClientFactory.class, DefaultCloudFoundryClientFactoryV2.INSTANCE);
	}


}
