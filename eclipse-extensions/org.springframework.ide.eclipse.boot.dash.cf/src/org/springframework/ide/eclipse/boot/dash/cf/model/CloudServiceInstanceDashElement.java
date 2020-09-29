/*******************************************************************************
 * Copyright (c) 2016, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.model;

import java.util.EnumSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFServiceInstance;
import org.springframework.ide.eclipse.boot.dash.cf.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.model.AbstractBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.AsyncDeletable;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springsource.ide.eclipse.commons.core.pstore.IPropertyStore;
import org.springsource.ide.eclipse.commons.core.pstore.PropertyStoreApi;
import org.springsource.ide.eclipse.commons.core.pstore.PropertyStores;

import reactor.core.publisher.Mono;

public class CloudServiceInstanceDashElement extends CloudDashElement<String> implements AsyncDeletable {

	private static final EnumSet<RunState> SERVICE_RUN_GOAL_STATES = EnumSet.noneOf(RunState.class);

	private static final BootDashColumn[] COLUMNS = {BootDashColumn.NAME, BootDashColumn.TAGS};

	private final CFServiceInstance service;
	private final PropertyStoreApi persistentProperties;

	public CloudServiceInstanceDashElement(AbstractBootDashModel parent, CFServiceInstance service, IPropertyStore modelStore) {
		super(parent, service.getName()+"@"+parent.getRunTarget().getId());
		this.service = service;
		IPropertyStore backingStore = PropertyStores.createSubStore("S"+getName(), modelStore);
		this.persistentProperties = PropertyStores.createApi(backingStore);
	}

	@Override
	public CloudFoundryBootDashModel getBootDashModel() {
		return (CloudFoundryBootDashModel) super.getBootDashModel();
	}

	@Override
	public Object getParent() {
		return getBootDashModel();
	}

	@Override
	public int getLivePort() {
		return -1;
	}

	@Override
	public String getLiveHost() {
		return null;
	}

	@Override
	public ILaunchConfiguration getActiveConfig() {
		return null;
	}

	@Override
	public void stop() throws Exception {
	}

	@Override
	public void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception {
	}

	@Override
	public void openConfig(UserInteractions ui) {
	}

	@Override
	public int getActualInstances() {
		return 0;
	}

	@Override
	public int getDesiredInstances() {
		return 0;
	}

	@Override
	public String getName() {
		return service.getName();
	}

	@Override
	public BootDashColumn[] getColumns() {
		return COLUMNS;
	}

	@Override
	public PropertyStoreApi getPersistentProperties() {
		return persistentProperties;
	}

	@Override
	public IProject getProject() {
		return null;
	}

	@Override
	public RunState getRunState() {
		return null;
	}

	@Override
	public String getUrl() {
		return service != null ? service.getDashboardUrl() : null;
	}

	public String getDocumentationUrl() {
		return service!=null ? service.getDocumentationUrl() : null;
	}

	public CFServiceInstance getCloudService() {
		return service;
	}

	public String getPlan() {
		CFServiceInstance s = getCloudService();
		return s==null?null:s.getPlan();
	}

	public String getService() {
		CFServiceInstance s = getCloudService();
		return s==null?null:s.getService();
	}

	public String getDescription() {
		CFServiceInstance s = getCloudService();
		return s==null?null:s.getDescription();
	}

	@Override
	public Mono<Void> deleteAsync() {
		CloudFoundryBootDashModel model = getBootDashModel();
		String serviceName = getName();
		return Mono.fromRunnable(this::cancelOperations)
		.then(getClient().deleteServiceAsync(serviceName))
		.doOnSuccess((ignore) -> model.removeService(serviceName));
	}

	private ClientRequests getClient() {
		return getTarget().getClient();
	}

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		return SERVICE_RUN_GOAL_STATES;
	}

	@Override
	public ImageDescriptor getCustomRunStateIcon() {
		return BootDashActivator.getDefault().getImageRegistry().getDescriptor(BootDashActivator.SERVICE_ICON);
	}

	@Override
	public Image getPropertiesTitleIconImage() {
		return BootDashActivator.getDefault().getImageRegistry().get(BootDashActivator.SERVICE_ICON);
	}
}
