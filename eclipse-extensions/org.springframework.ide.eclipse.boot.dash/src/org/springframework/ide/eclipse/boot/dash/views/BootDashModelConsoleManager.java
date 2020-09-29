/*******************************************************************************
 * Copyright (c) 2015, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;

import org.eclipse.ui.console.IConsole;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.console.LogType;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;

/**
 * Console manager for elements in a {@link BootDashModel}.
 * <p/>
 * Each {@link BootDashModel} should have its own console manager.
 *
 */
public abstract class BootDashModelConsoleManager {

	/**
	 * Opens the console for the given element. If console does not exist, a new
	 * one will be created.
	 *
	 * @param element
	 * @throws Exception
	 *             if failure occurred while opening console (e.g. failed to
	 *             create console, underlying process is terminated, etc..)
	 */
	public abstract void showConsole(App element) throws Exception;

	/**
	 * Write a message to an EXISTING console for the associated element.
	 *
	 * @param element
	 * @param message
	 */
	public void writeToConsole(App element, String message, LogType type) throws Exception {
		if (message != null) {
			String bootMessage = asBootDashLog(message);
			doWriteToConsole(element, bootMessage, type);
		}
	}

	protected abstract void doWriteToConsole(App element, String bootDashMessage, LogType type)
			throws Exception;

	/**
	 * Resets console (including possibly clearing contents) without destroying
	 * the console.
	 * <p/>
	 * This allows "active" consoles to remain alive but display updated
	 * information
	 *
	 * @param appName
	 */
	public abstract void resetConsole(App element);

	public abstract void terminateConsole(App element) throws Exception;

	public abstract void reconnect(App element) throws Exception;

	public abstract IConsole safeGetOrCreateConsole(App element);

	protected String asBootDashLog(String message) {
		Date date = new Date(System.currentTimeMillis());
		String dateVal = DateFormat.getDateTimeInstance().format(date);
		StringWriter writer = new StringWriter();
		writer.append('[');
		writer.append(dateVal);
		writer.append(' ');
		writer.append('-');
		writer.append(' ');
		writer.append("Boot Dashboard");
		writer.append(']');
		writer.append(' ');
		writer.append('-');
		writer.append(' ');
		writer.append(message);
		writer.append('\n');
		return writer.toString();
	}
}
