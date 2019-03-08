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
package org.springframework.ide.vscode.manifest.yaml;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ide.vscode.commons.cloudfoundry.client.CFServiceInstance;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTarget;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargetCache;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.yaml.schema.BasicYValueHint;
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;

public class ManifestYamlCFServicesProvider extends AbstractCFHintsProvider {

	public ManifestYamlCFServicesProvider(CFTargetCache cache) {
		super(cache);
	}

	@Override
	public Collection<YValueHint> getHints(CFTarget cfTarget) throws Exception {
		List<CFServiceInstance> services = cfTarget.getServices();
		if (services == null) {
			return Collections.emptyList();
		} else {
			Renderable targetLabel = Renderables.text(cfTarget.getLabel());
			return services.stream()
					.map(service -> new BasicYValueHint(service.getName(), getServiceLabel(cfTarget, service)).setDocumentation(targetLabel))
					.collect(Collectors.toList());
		}
	}

	@Override
	protected String getTypeName() {
		return "Service";
	}

	protected final String getServiceLabel(CFTarget cfClientTarget, CFServiceInstance service) {
		return service.getName() + " - " + service.getPlan();
	}
}
