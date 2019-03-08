/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client;

/**
 * Factory to create CF "Entities" like CF services, buildpacks, etc..
 */
public class CFEntities {

	public static CFBuildpack createBuildpack(String name) {
		return new CFBuildpackImpl(name);
	}

	public static CFServiceInstance createServiceInstance(String name, String service, String plan,
			String documentationUrl, String description, String dashboardUrl) {
		return new CFServiceInstanceImpl(name, service, plan, documentationUrl, description, dashboardUrl);
	}

	public static CFServiceInstance createServiceInstance(String name, String service, String plan) {
		return new CFServiceInstanceImpl(name, service, plan, /* doc url */ null, /* description */ null,
				/* dasboard Url */ null);
	}

	public static CFDomain createDomain(String name) {
		return new CFDomainImpl(name);
	}
}
