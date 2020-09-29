/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;
import org.springsource.ide.eclipse.commons.internal.core.commandhistory.CommandHistory;


/**
 * Provides access to {@link ICommandHistory} implementations.
 * @author Christian Dupuis
 * @author Kris De Volder
 * @since 2.5.0
 */
public class CommandHistoryProvider {

	private static final Map<String, ICommandHistory> COMMAND_HISTORIES = new HashMap<String, ICommandHistory>();

	public static synchronized ICommandHistory getCommandHistory(String historyId, String natureId) {
		String key = getKey(historyId, natureId);
		if (COMMAND_HISTORIES.containsKey(key)) {
			return COMMAND_HISTORIES.get(key);
		}
		else {
			try {
				CommandHistory commandHistory = new CommandHistory(historyId, natureId, true);
				COMMAND_HISTORIES.put(key, commandHistory);
				return commandHistory;
			}
			catch (CoreException e) {
				CorePlugin.log(
						"Warning: couldn't get persistent command history, using non-persistent history instead", e);
				return new CommandHistory(historyId, natureId);
			}
		}
	}

	private static String getKey(String historyId, String natureId) {
		Assert.isNotNull(historyId);
		Assert.isNotNull(natureId);
		return new StringBuilder().append(historyId).append("_").append(natureId).toString();
	}
}
