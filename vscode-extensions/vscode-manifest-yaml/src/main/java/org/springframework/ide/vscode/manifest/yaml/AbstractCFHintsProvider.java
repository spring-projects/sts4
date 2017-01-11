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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Provider;

import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTarget;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargetsFactory;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.yaml.schema.BasicYValueHint;
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;

public abstract class AbstractCFHintsProvider implements Provider<Collection<YValueHint>> {

	public static final String EMPTY_VALUE = "";
	protected final CFTargetsFactory targetsFactory;

	private static final Logger logger = Logger.getLogger(AbstractCFHintsProvider.class.getName());

	public AbstractCFHintsProvider(CFTargetsFactory targetsFactory) {
		Assert.isNotNull(targetsFactory);
		this.targetsFactory = targetsFactory;
	}

	@Override
	public Collection<YValueHint> get() {
		Collection<YValueHint> hints = new ArrayList<>();
		List<CFTarget> targets = null;
		try {
			targets = targetsFactory.getTargets();
		} catch (Throwable e) {
			// Don't throw exception. Just log, and instead show a "no targets"
			// hint below
			logger.log(Level.SEVERE, e.getMessage(), e);
		}

		if (targets == null || targets.isEmpty()) {
			// TODO: Probably a wrong thing to do, but for now show that
			// there are
			// no targets as a "hint" so that it appears
			// in CA UI
			hints.add(new BasicYValueHint(EMPTY_VALUE, targetsFactory.noTargetsMessage()));
		} else {
			try {
				Collection<YValueHint> resolvedHints = getHints(targets);
				hints.addAll(resolvedHints);
			} catch (Exception e) {
				throw ExceptionUtil.unchecked(e);
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
