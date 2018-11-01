/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.kubernetes.deployer;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class DeploymentResult {
	
	public static final DeploymentResult EMPTY = new DeploymentResult(ImmutableList.of());
	
	private final List<String> uris;
	
	public DeploymentResult(List<String> uris) {
		this.uris = ImmutableList.copyOf(uris);
	}

	public List<String> getUris() {
		return uris;
	}

}
