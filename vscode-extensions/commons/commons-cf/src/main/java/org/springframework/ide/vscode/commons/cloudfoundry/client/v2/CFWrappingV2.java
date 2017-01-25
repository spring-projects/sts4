/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.v2;

import org.cloudfoundry.operations.buildpacks.Buildpack;
import org.cloudfoundry.operations.services.ServiceInstance;
import org.cloudfoundry.operations.services.ServiceInstanceType;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFEntities;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFServiceInstance;

/**
 * Various helper methods to 'wrap' objects returned by CF client into our own
 * types, so that we do not directly expose library types to our code.
 *
 * @author Kris De Volder
 */
public class CFWrappingV2 {


	public static CFBuildpack wrap(Buildpack buildpack) {
		String name = buildpack.getName();
		return CFEntities.createBuildpack(name);
	}
	
	public static CFServiceInstance wrap(ServiceInstance serviceInstance) {
		String name = serviceInstance.getName();
		String plan = serviceInstance.getPlan();
		String dashboardUrl = serviceInstance.getDashboardUrl();
		String service = serviceInstance.getType() == ServiceInstanceType.USER_PROVIDED ? "user-provided"
				: serviceInstance.getService();
		String description = serviceInstance.getDescription();
		String documentationUrl = serviceInstance.getDocumentationUrl();
		return CFEntities.createServiceInstance(name, service, plan, documentationUrl, description, dashboardUrl);
	}

}
