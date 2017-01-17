/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client;

public class CFServiceInstanceImpl implements CFServiceInstance {

	private final String name;
	private final String plan;
	private final String dashboardUrl;
	private final String service;
	private final String description;
	private final String documentationUrl;

	public CFServiceInstanceImpl(String name, String plan, String dashboardUrl, String service, String description,
			String documentationUrl) {
		this.name = name;
		this.plan = plan;
		this.dashboardUrl = dashboardUrl;
		this.service = service;
		this.description = description;
		this.documentationUrl = documentationUrl;
	}

	public CFServiceInstanceImpl(String name) {
		this(name, null, null, null, null, null);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getPlan() {
		return this.plan;
	}

	@Override
	public String getDashboardUrl() {
		return this.dashboardUrl;
	}

	@Override
	public String getService() {
		return this.service;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public String getDocumentationUrl() {
		return this.documentationUrl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dashboardUrl == null) ? 0 : dashboardUrl.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((documentationUrl == null) ? 0 : documentationUrl.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((plan == null) ? 0 : plan.hashCode());
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CFServiceInstanceImpl other = (CFServiceInstanceImpl) obj;
		if (dashboardUrl == null) {
			if (other.dashboardUrl != null)
				return false;
		} else if (!dashboardUrl.equals(other.dashboardUrl))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (documentationUrl == null) {
			if (other.documentationUrl != null)
				return false;
		} else if (!documentationUrl.equals(other.documentationUrl))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (plan == null) {
			if (other.plan != null)
				return false;
		} else if (!plan.equals(other.plan))
			return false;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!service.equals(other.service))
			return false;
		return true;
	}

}