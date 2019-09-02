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

import java.util.List;

import com.google.common.collect.ImmutableList;

public class LivePropertySource {

	private final List<LiveProperty> properties;
	private final String sourceName;

	public LivePropertySource(String sourceName, List<LiveProperty> properties) {
		this.sourceName = sourceName;
		this.properties = properties != null ? ImmutableList.copyOf(properties) : ImmutableList.of();
	}

	public String getSourceName() {
		return this.sourceName;
	}

	public LiveProperty getProperty(String propertyName) {
		for (LiveProperty liveProperty : properties) {
			if (liveProperty.getProperty().equals(propertyName)) {
				return liveProperty;
			}
		}
		return null;
	}

}
