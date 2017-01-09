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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTarget;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargetsFactory;
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;

import com.google.common.collect.Lists;

public abstract class AbstractCFHintsProvider implements Provider<Collection<YValueHint>> {

	private final CFTargetsFactory targetsFactory;

	public AbstractCFHintsProvider(CFTargetsFactory targetsFactory) {
		this.targetsFactory = targetsFactory;
	}

	/**
	 * 
	 * @return non-null list of targets. May be empty if no targets available.
	 * @throws Exception
	 *             if error when creating the targets
	 */
	protected List<CFTarget> getUpdatedCFTargets() throws Exception {
		if (targetsFactory != null) {
			// Do not cache the targets. They may change (e.g. if using cf
			// CLI config, if CLI targets another API or org/space, the list
			// of targets fetched from the factory also
			// needs to be up-to-date)
			List<CFTarget> updatedTargets = targetsFactory.getTargets();
			if (updatedTargets != null) {
				return Lists.newArrayList(updatedTargets.iterator());
			}
		}
		return Collections.emptyList();
	}
}
