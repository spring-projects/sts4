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
 * Package-private. 
 * <p/>
 * Use {@link CFEntities} public API to create instance
 *
 */
class CFServiceInstanceImpl implements CFServiceInstance {

	private String service;
	private String plan;
	private String name;
	private String documentationUrl;
	private String description;
	private String dashboardUrl;

	public CFServiceInstanceImpl(String name, String service, String plan, String documentationUrl, String description,
			String dashboardUrl) {
		this.name = name;
		this.service = service;
		this.plan = plan;
		this.documentationUrl = documentationUrl;
		this.description = description;
		this.dashboardUrl = dashboardUrl;
	}

	@Override
	public String getService() {
		return service;
	}

	@Override
	public String getPlan() {
		return plan;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDocumentationUrl() {
		return documentationUrl;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getDashboardUrl() {
		return dashboardUrl;
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
