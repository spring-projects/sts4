/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class LiveProperties {

	private final List<LivePropertySource> sources;

	public LiveProperties(List<LivePropertySource> sources) {
		this.sources = sources != null ? ImmutableList.copyOf(sources) : ImmutableList.of();
	}

	public List<LiveProperty> getProperties(String propertyName) {
		List<LiveProperty> foundProperties = new ArrayList<>();
		for (LivePropertySource source : sources) {
			LiveProperty property = source.getProperty(propertyName);
			if (property != null) {
				foundProperties.add(property);
			}
		}
		return foundProperties;
	}

}
