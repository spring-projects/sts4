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

import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTarget;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargetCache;
import org.springframework.ide.vscode.commons.yaml.schema.BasicYValueHint;
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;

public class ManifestYamlCFBuildpacksProvider extends AbstractCFHintsProvider {

	public ManifestYamlCFBuildpacksProvider(CFTargetCache cache) {
		super(cache);
	}

	@Override
	public Collection<YValueHint> getHints(List<CFTarget> targets) throws Exception {
		List<YValueHint> hints = new ArrayList<>();
		for (CFTarget cfTarget : targets) {

			List<CFBuildpack> buildpacks = cfTarget.getBuildpacks();
			if (buildpacks != null) {
				for (CFBuildpack buildpack : buildpacks) {
					String name = buildpack.getName();
					String label = getBuildpackLabel(cfTarget, buildpack);
					YValueHint hint = new BasicYValueHint(name, label);
					if (!hints.contains(hint)) {
						hints.add(hint);
					}
				}
			}
		}

		return hints;
	}

	protected String getBuildpackLabel(CFTarget target, CFBuildpack buildpack) {
		return buildpack.getName() + " (" + target.getName() + ")";
	}
}
