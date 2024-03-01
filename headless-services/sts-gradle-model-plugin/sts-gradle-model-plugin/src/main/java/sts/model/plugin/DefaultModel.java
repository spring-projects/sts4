/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package sts.model.plugin;

import java.io.Serializable;

class DefaultModel implements StsToolingModel, Serializable {
	
	private static final long serialVersionUID = 725626813912256658L;
	
	String version;
	String group;
	String artifact;

	@Override
	public String version() {
		return version;
	}

	@Override
	public String group() {
		return group;
	}

	@Override
	public String artifact() {
		return artifact;
	}

}
