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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.boot.dash.azure.BootDashAzurePlugin;
import org.springframework.ide.eclipse.boot.dash.azure.client.STSAzureClient;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.AbstractRemoteRunTargetType;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JobUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.gson.Gson;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.implementation.ServiceResourceInner;
import com.microsoft.azure.management.resources.Subscription;

public class AzureRunTargetType extends AbstractRemoteRunTargetType<AzureTargetParams> {

	public static String getResourceGroupName(String serviceId) {
		// Example clusterId="/subscriptions/9036e83e-2238-42a4-9b2a-ecd80d4cc38d/resourceGroups/resource-test-dc/providers/Microsoft.AppPlatform/Spring/piggymetrics"
		String[] parts = StringUtils.splitPreserveAllTokens(serviceId, '/');
		return parts[4];
	}

	public static String getServiceName(String serviceId) {
		// Example clusterId="/subscriptions/9036e83e-2238-42a4-9b2a-ecd80d4cc38d/resourceGroups/resource-test-dc/providers/Microsoft.AppPlatform/Spring/piggymetrics"
		String[] parts = StringUtils.splitPreserveAllTokens(serviceId, '/');
		return parts[8];
	}


	public AzureRunTargetType(SimpleDIContext injections) {
		super(injections, "Azure Spring Cloud");
	}

	@Override
	public CompletableFuture<?> openTargetCreationUi(LiveSetVariable<RunTarget> targets) {
		return JobUtil.runInJob("Azure Target Creation", mon -> {
			STSAzureClient client = login(ui());
			if (client!=null) {
				Subscription sub = chooseSubscription(client);
				if (sub!=null) {
					client.setSubscription(sub);
					List<ServiceResourceInner> clusters = client.getSpringServiceClient().getAvailableClusters();
					if (clusters.isEmpty()) {
						ui().errorPopup("No Azure Spring Cloud Clusters",
								"We did not find any existing Azure Spring Cloud Clusters under subscription " +
								sub.displayName()+".\n\n" +
								"Please first create a cluster using 'az' CLI and then try again"
						);
					} else {
						ServiceResourceInner cluster = ui().chooseElement("Choose a cluster", "Choose a cluster", clusters, c -> {
							return getResourceGroupName(c.id()) +" / " + c.name();
						});
						if (cluster!=null) {
							client.setCluster(cluster);
							targets.add(new AzureRunTarget(this, client));
						}
					}
				}
			}
		});
	}

	private Subscription chooseSubscription(STSAzureClient client) {
		PagedList<Subscription> subs = client.getSubsriptions();
		if (subs.isEmpty()) {
			ui().errorPopup("No Azure Subscriptions Found", "You need an Azure Subscription, but none was "
					+ "found for the authenticated user. Please sign up for an Azure subscription or "
					+ "try again and authenticate as a different user.");
		} else {
			return ui().chooseElement("Choose a subscription", "Choose a subscription", subs, Subscription::displayName);
		}
		return null;
	}

	@Override
	public RunTarget<AzureTargetParams> createRunTarget(AzureTargetParams properties) {
		return new AzureRunTarget(this, properties);
	}

	@Override
	public ImageDescriptor getIcon() {
		return BootDashAzurePlugin.getImageDescriptor("/icons/azure.png");
	}

	@Override
	public ImageDescriptor getDisconnectedIcon() {
		return BootDashAzurePlugin.getImageDescriptor("icons/azure-inactive.png");
	}

	@Override
	public AzureTargetParams parseParams(String serializedTargetParams) {
		Gson gson = new Gson();
		return gson.fromJson(serializedTargetParams, AzureTargetParams.class);
	}

	@Override
	public String serialize(AzureTargetParams targetParams) {
		Gson gson = new Gson();
		return gson.toJson(targetParams);
	}

	/**
	 * Connect to azure with credentials obtained by prompting the user
	 *
	 * @return When login is successful, a client in authenticated (but not targeted) state; otherwise null.
	 */
	public STSAzureClient login(UserInteractions ui) {
		try {
			if (ui.confirmOperation("Obtaining Credentials via OAuth",
					"To access your Azure Spring Cloud subscriptions, resources and services, " +
					"STS needs to be authorized by you. A web browser will now be opened for that purpose.")
			) {
				STSAzureClient client = new STSAzureClient();
				client.authenticate();
				return client;
			}
		} catch (Exception e) {
			Log.log(e);
			ui.errorPopup("Authentication failed", ExceptionUtil.getMessage(e));
		}
		return null;
	}

}
