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
package org.springframework.ide.eclipse.boot.dash.console;

import org.eclipse.swt.SWT;

public class LogType {
	/*
	 * Local messages types
	 */
	public static final LogType STDOUT = new LogType(SWT.COLOR_DARK_BLUE);
	public static final LogType STDERROR = new LogType(SWT.COLOR_RED);
	public static final LogType APP_ERROR = new LogType(SWT.COLOR_RED);
	public static final LogType APP_OUT = new LogType(SWT.COLOR_DARK_GREEN);

	private final int displayColour;

	public LogType(int displayColour) {
		this.displayColour = displayColour;
	}

	public int getDisplayColour() {
		return this.displayColour;
	}
}
