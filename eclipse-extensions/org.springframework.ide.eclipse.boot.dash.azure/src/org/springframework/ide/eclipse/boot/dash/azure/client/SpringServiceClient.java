/**
 * Copyright (c) Microsoft Corporation and others. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package org.springframework.ide.eclipse.boot.dash.azure.client;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ide.eclipse.boot.dash.azure.runtarget.AzureRunTargetType;
import org.springframework.ide.eclipse.boot.dash.azure.runtarget.AzureTargetParams;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.implementation.AppPlatformManager;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.implementation.ServiceResourceInner;
import com.microsoft.rest.LogLevel;

public class SpringServiceClient {

    protected static final String NO_CLUSTER = "No cluster named %s found in subscription %s";

    private String subscriptionId;
    private AppPlatformManager springManager;

    public SpringServiceClient(AzureTokenCredentials azureTokenCredentials, String subscriptionId, String userAgent) {
        this(azureTokenCredentials, subscriptionId, userAgent, LogLevel.NONE);
    }

    public SpringServiceClient(AzureTokenCredentials azureTokenCredentials, String subscriptionId, String userAgent, LogLevel logLevel) {
        subscriptionId = StringUtils.isEmpty(subscriptionId) ? azureTokenCredentials.defaultSubscriptionId() : subscriptionId;
        this.subscriptionId = subscriptionId;
        this.springManager = AppPlatformManager.configure()
                .withLogLevel(logLevel)
                .withUserAgent(userAgent)
                .authenticate(azureTokenCredentials, subscriptionId);
    }

//    public SpringAppClient newSpringAppClient(String subscriptionId, String cluster, String app) {
//        final SpringAppClient.Builder builder = new SpringAppClient.Builder();
//        return builder.withSubscriptionId(subscriptionId)
//                .withSpringServiceClient(this)
//                .withClusterName(cluster)
//                .withAppName(app)
//                .build();
//    }
//
//    public SpringAppClient newSpringAppClient(SpringConfiguration configuration) {
//        return newSpringAppClient(configuration.getSubscriptionId(), configuration.getClusterName(), configuration.getAppName());
//    }

    public ServiceResourceInner getClusterById(String id) {
    	String resourceGroupName = AzureRunTargetType.getResourceGroupName(id);
    	String serviceName = AzureRunTargetType.getServiceName(id);

    	return getSpringManager().inner().services()
    			.getByResourceGroup(resourceGroupName, serviceName);
    }

    public List<ServiceResourceInner> getAvailableClusters() {
        final PagedList<ServiceResourceInner> clusterList = getSpringManager().inner().services().list();
        clusterList.loadAll();
        return new ArrayList<>(clusterList);
    }

    public ServiceResourceInner getClusterByName(String cluster) {
        final List<ServiceResourceInner> clusterList = getAvailableClusters();
        return clusterList.stream().filter(appClusterResourceInner -> appClusterResourceInner.name().equals(cluster))
                .findFirst()
                .orElseThrow(() -> new InvalidParameterException(String.format(NO_CLUSTER, cluster, subscriptionId)));
    }

    public String getResourceGroupByCluster(String clusterName) {
        final ServiceResourceInner cluster = getClusterByName(clusterName);
        final String[] attributes = cluster.id().split("/");
        return attributes[ArrayUtils.indexOf(attributes, "resourceGroups") + 1];
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public AppPlatformManager getSpringManager() {
        return springManager;
    }
}
