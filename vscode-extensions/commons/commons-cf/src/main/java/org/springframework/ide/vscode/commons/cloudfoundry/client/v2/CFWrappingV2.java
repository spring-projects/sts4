/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
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
 * Various helper methods to 'wrap' objects returned by CF client into
 * our own types, so that we do not directly expose library types to our
 * code.
 *
 * @author Kris De Volder
 */
public class CFWrappingV2 {
	

    private static final Logger logger = Logger.getLogger(DefaultClientRequestsV2.class.getName());


	public static CFBuildpack wrap(BuildpackResource rsrc) {
		String name = rsrc.getEntity().getName();
		return new CFBuildpack() {
			@Override
			public String getName() {
				return name;
			}
		};
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

	public static CFServiceInstance wrap(final ServiceInstance service) {
		return new CFServiceInstance() {

			@Override
			public String getName() {
				return service.getName();
			}

			@Override
			public String getPlan() {
				return service.getPlan();
			}

			@Override
			public String getDashboardUrl() {
				return service.getDashboardUrl();
			}

			@Override
			public String getService() {
				if (service.getType()==ServiceInstanceType.USER_PROVIDED) {
					return "user-provided";
				} else {
					return service.getService();
				}
			}

			@Override
			public String getDescription() {
				return service.getDescription();
			}

			@Override
			public String getDocumentationUrl() {
				return service.getDocumentationUrl();
			}
		};
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
		return new CFBuildpack() {
			@Override
			public String getName() {
				return name;
			}
		};
	}

}
