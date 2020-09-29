/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.model;

import org.springframework.ide.eclipse.boot.dash.cf.client.CFServiceInstance;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springsource.ide.eclipse.commons.core.pstore.IPropertyStore;

public class CloudDashElementFactory {

	private final IPropertyStore modelStore;

	private final CloudFoundryBootDashModel model;

	public CloudDashElementFactory(BootDashModelContext context, IPropertyStore modelStore,
			CloudFoundryBootDashModel model) {
		this.modelStore = modelStore;
		this.model = model;
	}

	public CloudServiceInstanceDashElement createService(CFServiceInstance service) {
		return new CloudServiceInstanceDashElement(model, service, modelStore);
	}
}
