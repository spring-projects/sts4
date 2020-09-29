/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.client;

import java.util.List;

import org.eclipse.core.runtime.Assert;

/**
 * A Cloud application with additional stats and instances information
 *
 * This should be removed. Use {@link CFApplicationDetail} instead. It contains
 * all the same infos.
 */
@Deprecated
public class CloudAppInstances {

	private final CFApplication app;
	private final List<CFInstanceStats> stats;

	public CloudAppInstances(CFApplication app, List<CFInstanceStats> stats) {
		Assert.isNotNull(app);
		this.app = app;
		this.stats = stats;
	}

	public CFApplication getApplication() {
		return app;
	}

	public List<CFInstanceStats> getStats() {
		return stats;
	}
}
