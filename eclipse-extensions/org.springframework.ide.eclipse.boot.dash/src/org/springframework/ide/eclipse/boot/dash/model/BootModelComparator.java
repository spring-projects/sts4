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
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.Comparator;

/**
 * Model is just a wrapper on a {@link RunTarget} so {@link BootModelComparator} should
 * just use {@link RunTarget} comparator.
 */
public class BootModelComparator implements Comparator<BootDashModel> {

	private Comparator<RunTarget> targetComparator;

	public BootModelComparator(Comparator<RunTarget> targetComparator) {
		this.targetComparator = targetComparator;
	}

	public int compare(BootDashModel model1, BootDashModel model2) {
		RunTarget t1 = model1.getRunTarget();
		RunTarget t2 = model2.getRunTarget();
		return targetComparator.compare(t1, t2);
	}
}
