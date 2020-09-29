/*******************************************************************************
 * Copyright (c) 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.api;

/**
 * Boot Dash has a console ui that provides a console
 * for each {@link App}. The {@link AppConsoleProvider} allows extension authors
 * to obtain a reference to the console for their {@link App}s and use it to write
 * messages into the console.
 */
public interface AppConsoleProvider {
	 AppConsole getConsole(App app);
}
