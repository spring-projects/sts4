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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Provider;

import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTarget;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargetCache;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.NoTargetsException;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.yaml.schema.BasicYValueHint;
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;

public abstract class AbstractCFHintsProvider implements Provider<Collection<YValueHint>> {

	public static final String EMPTY_VALUE = "";
	protected final CFTargetCache targetCache;

	private static final Logger logger = Logger.getLogger(AbstractCFHintsProvider.class.getName());

	public AbstractCFHintsProvider(CFTargetCache targetCache) {
		Assert.isNotNull(targetCache);
		this.targetCache = targetCache;
	}

	@Override
	public Collection<YValueHint> get() {
		Collection<YValueHint> hints = new ArrayList<>();
		// TODO: Probably not the most ideal thing to do, but for now show any
		// CF errors
		// in the CA UI, as well as cases where there are no targets
		try {
			List<CFTarget> targets = targetCache.getOrCreate();
			Collection<YValueHint> resolvedHints = getHints(targets);
			hints.addAll(resolvedHints);
		} catch (Throwable e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			// Don't propagate exception as to allow the CA to be displayed to
			// the
			// user.
			if (ExceptionUtil.getThrowable(e, NoTargetsException.class) != null) {
				hints.add(new BasicYValueHint(EMPTY_VALUE, e.getMessage()));
			} else if (ExceptionUtil.getThrowable(e, IOException.class) != null) {
				hints.add(new BasicYValueHint(EMPTY_VALUE,
						"Connection failure to Cloud Foundry. Please check the log for more details."));
			} else {
				hints.add(new BasicYValueHint(EMPTY_VALUE,
						"Failed to fetch values from Cloud Foundry. Please check the log for more details."));
			}
		}
		return hints;
	}

	/**
	 * 
	 * @return non-null list of hints. Return empty if no hints available
	 */
	abstract protected Collection<YValueHint> getHints(List<CFTarget> targets) throws Exception;

}
