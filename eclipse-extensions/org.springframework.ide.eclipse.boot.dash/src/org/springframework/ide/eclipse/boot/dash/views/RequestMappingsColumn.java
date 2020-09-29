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
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.swt.SWT;

public enum RequestMappingsColumn {

	PATH("Path", 80),
	SRC("Source", 100);

	private int defaultWidth;
	private String label;

	private RequestMappingsColumn(String label, int defaultWidth) {
		this.defaultWidth = defaultWidth;
		this.label= label;
	}

	public String getLabel() {
		return label;
	}

	public int getAlignment() {
		return SWT.LEFT;
	}

	public int getDefaultWidth() {
		return defaultWidth;
	}

}
