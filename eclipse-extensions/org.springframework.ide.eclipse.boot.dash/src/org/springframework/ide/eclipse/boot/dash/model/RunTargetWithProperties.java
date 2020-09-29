/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;

/**
 * A run target that contains a list of properties that describe the run target.
 * Not all run targets define properties (e.g. the local run target)
 *
 */
public interface RunTargetWithProperties<Params> extends RunTarget<Params> {

	public TargetProperties getTargetProperties();

	/**
	 *
	 * @return true if the target requires credentials. False otherwise
	 */
	public abstract boolean requiresCredentials();
}
