/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.deployment;

import java.util.List;

import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

import com.google.common.collect.ImmutableList;

public class UnsupportedPushProperties {



	public UnsupportedPushProperties() {
	}

	/**
	 * Check for unsupported push properties in the given deployment properties. Either allow the
	 * caller of this check to continue if unsupported properties are found, or
	 * cancel. If no unsupported properties are found, nothing happens.
	 *
	 * @param ui
	 * @param cde
	 * @param deploymentProperties
	 * @throws OperationCanceledException if operation is cancelled
	 */
	public void allowOrCancelIfFound(UserInteractions ui,
			CloudApplicationDeploymentProperties deploymentProperties) throws OperationCanceledException {
		List<String> unsupportedProperties = findUnsupportedProperties(deploymentProperties);
		if (!unsupportedProperties.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			builder.append(
					"The following properties are not currently supported when pushing an application to Cloud Foundry. These properties will be ignored. Continue with deployment?");
			builder.append('\n');

			for (String prop : unsupportedProperties) {
				builder.append('\n');
				builder.append("- ");
				builder.append(prop);
			}
			if (!ui.confirmOperation("Unsupported Push Properties", builder.toString())) {
				throw new OperationCanceledException();
			}
		}
	}

	/**
	 * Check the given deployment properties for unsupported properties. Return
	 * properties found that are unsupported, or empty list if nothing is found.
	 *
	 * @param properties
	 * @return
	 */
	public List<String> findUnsupportedProperties(CloudApplicationDeploymentProperties properties) {
		if (properties != null) {
			if (properties.getBuildpacks() != null && !properties.getBuildpacks().isEmpty()) {
				return ImmutableList.of("buildpacks");
			}
		}
		return ImmutableList.of();
	}

}
