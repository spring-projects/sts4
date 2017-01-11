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
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargetsFactory;
import org.springframework.ide.vscode.commons.yaml.schema.BasicYValueHint;
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;

public class ManifestYamlCFServicesProvider extends AbstractCFHintsProvider {

	public ManifestYamlCFServicesProvider(CFTargetsFactory targetsFactory) {
		super(targetsFactory);
	}

	@Override
	public Collection<YValueHint> getHints(List<CFTarget> targets) throws Exception {
		List<YValueHint> hints = new ArrayList<>();

		for (CFTarget cfTarget : targets) {
			List<CFServiceInstance> services = cfTarget.getClientRequests().getServices();
			if (services != null) {
				for (CFServiceInstance service : services) {
					String name = service.getName();
					String label = getServiceLabel(cfTarget, service);
					YValueHint hint = new BasicYValueHint(name, label);
					if (!hints.contains(hint)) {
						hints.add(hint);
					}
				}
			}
		}

		if (hints.isEmpty()) {
			hints.add(new BasicYValueHint(EMPTY_VALUE, "No Cloud Foundry service instances available"));
		}
		return hints;
	}

	private String getServiceLabel(CFTarget cfClientTarget, CFServiceInstance service) {
		return service.getName() + " - " + service.getPlan() + " (" + cfClientTarget.getParams().getOrgName() + " - "
				+ cfClientTarget.getParams().getSpaceName() + ")";
	}
}
