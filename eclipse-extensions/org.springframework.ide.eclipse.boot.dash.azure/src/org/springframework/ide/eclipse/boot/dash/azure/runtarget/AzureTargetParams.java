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

import com.microsoft.azure.auth.AzureCredential;

/**
 * Json serialisation friendly object that contains all the data
 * we need to establish connection to an Azure Spring Cloud cluster.
 */
public class AzureTargetParams {

	private AzureCredential credentials;
	private String subscriptionId;
	private String subscriptionName;
	private String clusterId;
	private String clusterName;

	public AzureTargetParams() {}

	public AzureTargetParams(AzureCredential credentials, String subscriptionId, String subscriptionName, String clusterId, String clusterName) {
		super();
		this.credentials = credentials;
		this.subscriptionId = subscriptionId;
		this.subscriptionName = subscriptionName;
		this.clusterId = clusterId;
		this.clusterName = clusterName;
	}

	public AzureCredential getCredentials() {
		return credentials;
	}

	public void setCredentials(AzureCredential credentials) {
		this.credentials = credentials;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getSubscriptionName() {
		return this.subscriptionName;
	}

	public void setSubscriptionName(String subscriptionName) {
		this.subscriptionName = subscriptionName;
	}
}
