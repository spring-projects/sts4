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

import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTarget;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargetCache;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.yaml.schema.BasicYValueHint;
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;

public class ManifestYamlCFBuildpacksProvider extends AbstractCFHintsProvider {

	public ManifestYamlCFBuildpacksProvider(CFTargetCache cache) {
		super(cache);
	}

	@Override
	public Collection<YValueHint> getHints(CFTarget cfTarget) throws Exception {
		List<CFBuildpack> buildpacks = cfTarget.getBuildpacks();
		if (buildpacks == null) {
			return Collections.emptyList();
		} else {
			Renderable targetLabel = Renderables.text(cfTarget.getLabel());
			return buildpacks.stream()
					.map(buildpack -> new BasicYValueHint(buildpack.getName(), getBuildpackLabel(cfTarget, buildpack)).setDocumentation(targetLabel))
					.collect(Collectors.toList());
		}
	}

	protected String getBuildpackLabel(CFTarget target, CFBuildpack buildpack) {
		return buildpack.getName();
	}

	@Override
	protected String getTypeName() {
		return "Buildpack";
	}
}
