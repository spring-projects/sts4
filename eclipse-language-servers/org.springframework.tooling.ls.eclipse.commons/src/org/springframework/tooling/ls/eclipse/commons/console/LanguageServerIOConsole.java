/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons.console;

import org.eclipse.ui.console.IOConsole;

public class LanguageServerIOConsole extends IOConsole {

	private static final String CONSOLE_TYPE = LanguageServerIOConsole.class.getName();

	public LanguageServerIOConsole(final String title) {
		super(title, CONSOLE_TYPE, null);
	}

}