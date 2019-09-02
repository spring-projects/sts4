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

public class LiveProperty {

	private String source;
	private String property;
	private String value;

	public String getSource() {
		return source;
	}

	public String getProperty() {
		return property;
	}

	public String getValue() {
		return value;
	}

	public static class LivePropertyBuilder {

		private LiveProperty liveProperty = new LiveProperty();

		public LivePropertyBuilder source(String source) {
			liveProperty.source = source;
			return this;
		}

		public LivePropertyBuilder property(String property) {
			liveProperty.property = property;
			return this;
		}

		public LivePropertyBuilder value(String value) {
			liveProperty.value = value;
			return this;
		}

		public LiveProperty build() {
			return liveProperty;
		}
	}

	public static LivePropertyBuilder builder() {
		return new LivePropertyBuilder();
	}

}
