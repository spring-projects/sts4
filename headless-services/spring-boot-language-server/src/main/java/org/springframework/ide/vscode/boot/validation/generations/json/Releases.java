/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations.json;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class Releases {

	private Release[] releases;

	public List<Release> getReleases() {
		return releases != null ? Arrays.asList(releases) : ImmutableList.of();
	}

}
