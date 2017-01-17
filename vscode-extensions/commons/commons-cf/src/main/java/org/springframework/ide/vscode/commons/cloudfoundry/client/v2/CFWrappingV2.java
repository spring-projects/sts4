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

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.cloudfoundry.client.v2.buildpacks.BuildpackResource;
import org.cloudfoundry.client.v2.domains.DomainResource;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.InstanceDetail;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.services.ServiceInstance;
import org.cloudfoundry.operations.services.ServiceInstanceType;
import org.cloudfoundry.operations.spaces.SpaceSummary;
import org.cloudfoundry.operations.stacks.Stack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFAppState;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFApplication;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFInstanceState;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFInstanceStats;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFOrganization;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFServiceInstance;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFSpace;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFStack;

import com.google.common.collect.ImmutableList;

/**
 * Various helper methods to 'wrap' objects returned by CF client into our own
 * types, so that we do not directly expose library types to our code.
 *
 * @author Kris De Volder
 */
public class CFWrappingV2 {

	private static final Logger logger = Logger.getLogger(DefaultClientRequestsV2.class.getName());

	public static CFBuildpack wrap(BuildpackResource rsrc) {
		String name = rsrc.getEntity().getName();
		return new CFBuildpackImpl(name);
	}

	public static CFApplicationDetail wrap(ApplicationDetail details, ApplicationExtras extras) {
		if (details!=null) {
			List<CFInstanceStats> instances = ImmutableList.copyOf(
				details.getInstanceDetails()
				.stream()
				.map(CFWrappingV2::wrap)
				.collect(Collectors.toList())
			);
			CFApplicationSummaryData summary = wrapSummary(details, extras);
			return new CFApplicationDetailData(
					summary,
					instances
			);
		}
		return null;
	}

	public static CFStack wrap(Stack stack) {
		if (stack!=null) {
			String name = stack.getName();
			return new CFStack() {
				@Override
				public String getName() {
					return name;
				}

				@Override
				public String toString() {
					return "CFStack("+name+")";
				}
			};
		}
		return null;
	}

	public static CFApplicationDetail wrap(
			CFApplicationSummaryData summary,
			ApplicationDetail details
	) {
		List<CFInstanceStats> instanceDetails = ImmutableList.copyOf(
				details
				.getInstanceDetails()
				.stream()
				.map(CFWrappingV2::wrap)
				.collect(Collectors.toList())
		);
		return new CFApplicationDetailData(summary, instanceDetails);
	}

	public static CFCloudDomain wrap(DomainResource domainRsrc) {
		if (domainRsrc!=null) {
			String name = domainRsrc.getEntity().getName();
			return new CFCloudDomain() {
				public String getName() {
					return name;
				}
				@Override
				public String toString() {
					return "CFCloudDomain("+name+")";
				}
			};
		}
		return null;
	}

	public static CFInstanceStats wrap(InstanceDetail instanceDetail) {
		return new CFInstanceStats() {
			@Override
			public CFInstanceState getState() {
				try {
					return CFInstanceState.valueOf(instanceDetail.getState());
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					return CFInstanceState.UNKNOWN;
				}
			}

			@Override
			public String toString() {
				return ""+getState();
			}
		};
	}

	private static CFApplicationSummaryData wrapSummary(ApplicationDetail app, ApplicationExtras extras) {
		CFAppState state;
		try {
			state = CFAppState.valueOf(app.getRequestedState());
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			state = CFAppState.UNKNOWN;
		}

		return new CFApplicationSummaryData(
				app.getName(),
				app.getInstances(),
				app.getRunningInstances(),
				app.getMemoryLimit(),
				UUID.fromString(app.getId()),
				app.getUrls(),
				state,
				app.getDiskQuota(),
				extras
		);
	}

	public static CFApplication wrap(ApplicationSummary app, ApplicationExtras extras) {
		CFAppState state;
		try {
			state = CFAppState.valueOf(app.getRequestedState());
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			state = CFAppState.UNKNOWN;
		}

		return new CFApplicationSummaryData(
				app.getName(),
				app.getInstances(),
				app.getRunningInstances(),
				app.getMemoryLimit(),
				UUID.fromString(app.getId()),
				app.getUrls(),
				state,
				app.getDiskQuota(),
				extras
		);
	}

	public static CFServiceInstance wrap(ServiceInstance service) {
		return new CFServiceInstanceImpl(service);
	}

	public static CFAppState wrapAppState(String s) {
		try {
			return CFAppState.valueOf(s);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return CFAppState.UNKNOWN;
		}
	}

	public static CFSpace wrap(OrganizationSummary org, SpaceSummary space) {
		return new CFSpace() {
			@Override
			public String getName() {
				return space.getName();
			}
			@Override
			public CFOrganization getOrganization() {
				return wrap(org);
			}
			@Override
			public UUID getGuid() {
				return UUID.fromString(space.getId());
			}

			@Override
			public String toString() {
				return "CFSpace("+org.getName()+" / "+getName()+")";
			}
		};
	}

	public static CFOrganization wrap(OrganizationSummary org) {
		return new CFOrganization() {
			@Override
			public String getName() {
				return org.getName();
			}

			@Override
			public UUID getGuid() {
				return UUID.fromString(org.getId());
			}
		};
	}

	public static CFBuildpack buildpack(String name) {
		return new CFBuildpackImpl(name);
	}

	public static class CFBuildpackImpl implements CFBuildpack {

		private final String name;

		public CFBuildpackImpl(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
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
			CFBuildpackImpl other = (CFBuildpackImpl) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

	}

	public static class CFServiceInstanceImpl implements CFServiceInstance {

		private final String name;
		private final String plan;
		private final String dashboardUrl;
		private final String service;
		private final String description;
		private final String documentationUrl;

		public CFServiceInstanceImpl(ServiceInstance serviceInstance) {
			this.name = serviceInstance.getName();
			this.plan = serviceInstance.getPlan();
			this.dashboardUrl = serviceInstance.getDashboardUrl();
			this.service = serviceInstance.getType() == ServiceInstanceType.USER_PROVIDED ? "user-provided"
					: serviceInstance.getService();
			this.description = serviceInstance.getDescription();
			this.documentationUrl = serviceInstance.getDocumentationUrl();
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

}
