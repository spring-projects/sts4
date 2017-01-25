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
package org.springframework.ide.vscode.manifest.yaml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.ide.vscode.commons.cloudfoundry.client.CFServiceInstance;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTarget;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargetCache;
import org.springframework.ide.vscode.commons.yaml.schema.BasicYValueHint;
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;

public class ManifestYamlCFServicesProvider extends AbstractCFHintsProvider {

	public ManifestYamlCFServicesProvider(CFTargetCache cache) {
		super(cache);
	}

	@Override
	public Collection<YValueHint> getHints(List<CFTarget> targets) throws Exception {

		// NOTE: empty list of services is a VALID result. A CF target may have
		// no service instances
		// created, so if empty list is returned from the client, then RETURN empty list. don't
		// return null
		// for empty services cases
		List<YValueHint> hints = new ArrayList<>();

		for (CFTarget cfTarget : targets) {
			List<CFServiceInstance> services = cfTarget.getServices();
			if (services != null && !services.isEmpty()) {

				for (CFServiceInstance service : services) {
					String name = service.getName();
					String label = getServiceLabel(cfTarget, service);
					YValueHint hint = new BasicYValueHint(name, label);
					if (!hints.contains(hint)) {
						hints.add(hint);
					}
				}
				return hints;
			}
		}

		return hints;
	}

	private String getServiceLabel(CFTarget cfClientTarget, CFServiceInstance service) {
		return service.getName() + " - " + service.getPlan() + " (" + cfClientTarget.getParams().getOrgName() + " - "
				+ cfClientTarget.getParams().getSpaceName() + ")";
	}
}
