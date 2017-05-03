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

import org.springframework.ide.vscode.commons.cloudfoundry.client.CFStack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTarget;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargetCache;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.yaml.schema.BasicYValueHint;
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;

public class ManifestYamlStacksProvider extends AbstractCFHintsProvider {

	public ManifestYamlStacksProvider(CFTargetCache targetCache) {
		super(targetCache);
	}

	@Override
	protected String getTypeName() {
		return "Stack";
	}

	@Override
	protected Collection<YValueHint> getHints(List<CFTarget> targets) throws Exception {
		// NOTE: empty list of services is a VALID result. A CF target may have
		// no service instances
		// created, so if empty list is returned from the client, then RETURN empty list. don't
		// return null
		// for empty services cases
		List<YValueHint> hints = new ArrayList<>();

		for (CFTarget cfTarget : targets) {
			List<CFStack> stacks = cfTarget.getStacks();
			Renderable targetLabel = Renderables.text(cfTarget.getLabel());
			if (stacks != null && !stacks.isEmpty()) {
				for (CFStack s : stacks) {
					String name = s.getName();
					String label = name;
					YValueHint hint = new BasicYValueHint(name, label)
							.setDocumentation(targetLabel);
					if (!hints.contains(hint)) {
						hints.add(hint);
					}
				}
				return hints;
			}
		}

		return hints;
	}


}
