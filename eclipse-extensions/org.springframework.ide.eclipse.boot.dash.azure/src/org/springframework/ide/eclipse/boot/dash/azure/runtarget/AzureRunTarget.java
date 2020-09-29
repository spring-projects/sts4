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
package org.springframework.ide.eclipse.boot.dash.azure.runtarget;

import java.util.Collection;

import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.azure.BootDashAzurePlugin;
import org.springframework.ide.eclipse.boot.dash.azure.client.STSAzureClient;
import org.springframework.ide.eclipse.boot.dash.azure.client.SpringServiceClient;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.RemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.AbstractRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.remote.GenericRemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

import com.google.common.collect.ImmutableSet;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.AppResource;

public class AzureRunTarget extends AbstractRunTarget<AzureTargetParams> implements RemoteRunTarget<SpringServiceClient, AzureTargetParams> {

	private final AzureTargetParams params;
	private final LiveVariable<SpringServiceClient> client = new LiveVariable<>();

	@Override
	public LiveExpression<SpringServiceClient> getClientExp() {
		return client;
	}

	/**
	 * Creates a target in 'connected' state.
	 */
	public AzureRunTarget(AzureRunTargetType type, STSAzureClient client) {
		this(type, client.getTargetParams());
		SpringServiceClient connection = client.getSpringServiceClient();
		this.client.setValue(connection);
	}

	/**
	 * Create a target in a not connected state, but with all the info needed to
	 * estabslish a connection (at a later time).
	 */
	public AzureRunTarget(AzureRunTargetType type, AzureTargetParams properties) {
		super(type, properties.getClusterId(), properties.getClusterName());
		this.params = properties;
	}

	@Override
	public RemoteBootDashModel createSectionModel(BootDashViewModel parent) {
		return new GenericRemoteBootDashModel<>(this, parent);
	}

	@Override
	public boolean canRemove() {
		return true;
	}

	@Override
	public boolean canDeployAppsFrom() {
		return false;
	}

	@Override
	public AzureTargetParams getParams() {
		return params;
	}

	@Override
	public void dispose() {
	}

	@Override
	public String getDisplayName() {
		return getResourceGroupName() + " : "+getClusterName() + " ["+getSubscriptionName()+"]";
	}

	private String getSubscriptionName() {
		return params.getSubscriptionName();
	}

	String getClusterName() {
		return params.getClusterName();
	}

	String getResourceGroupName() {
		String clusterId = params.getClusterId();
		return AzureRunTargetType.getResourceGroupName(clusterId);
	}

	@Override
	public Collection<App> fetchApps() {
		SpringServiceClient client = this.getClient();
		if (client!=null) {
			String resourceGroupName = getResourceGroupName();
			String serviceName = getClusterName();
			Iterable<AppResource> apps = client.getSpringManager().apps().listAsync(resourceGroupName, serviceName).toBlocking().toIterable();
			ImmutableSet.Builder<App> builder = ImmutableSet.builder();
			for (AppResource appResource : apps) {
				System.out.println(appResource);
				System.out.println(appResource.properties().provisioningState());
				builder.add(new AzureApp(this, appResource));
			}
			return builder.build();
		}
		return ImmutableSet.of();
	}

	@Override
	public void disconnect() {
		//SpringServiceClient c = client.getValue();
		client.setValue(null);
	}

	@Override
	public void connect(ConnectMode mode) throws Exception {
		STSAzureClient c = new STSAzureClient();
		c.reconnect(getParams());
		client.setValue(c.getSpringServiceClient());
	}
}
