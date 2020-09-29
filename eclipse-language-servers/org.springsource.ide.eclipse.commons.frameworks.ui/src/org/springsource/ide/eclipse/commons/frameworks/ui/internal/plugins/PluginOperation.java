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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.plugins;

/**
 * Operations that can be performed on a plugin in the plugin manager. Note that
 * some operations may have dual roles depending on the state of the plugin. See
 * documentation below for each operation type. Some operations require plugin
 * selection in the manager, others do not.
 * 
 * @author nisingh
 * 
 */
public enum PluginOperation {
	/**
	 * Refreshes the list of plugins. This operation obtains an updated list of
	 * plugins from Grails, and may therefore involve a remote call.
	 */
	REFRESH("Refresh"),

	/**
	 * Install a selected group of plugins, IF the plugins have not been
	 * installed yet this operation should be disabled for plugins that are
	 * already installed or marked for update. To update to a newer or downgrade
	 * to an older version of an installed plugin, use the Update operation instead.
	 * <p>
	 * In addition, this operation can be used to UNDO an Uninstall operation
	 * that has been scheduled for a particular plugin.
	 * </p>
	 */
	INSTALL("Install"),

	/**
	 * Uninstall a selected group of plugins, IF the plugins are already
	 * installed. This operation should be disabled for plugins that are not
	 * installed.
	 * <p>
	 * In addition, this operation can be used to UNDO an Install or Update operation
	 * that has been scheduled for a particular plugin.
	 * </p>
	 */
	UNINSTALL("Uninstall"),

	/**
	 * Selects a particular version of a plugin that is currently installed for update.
	 * This operation should be disabled if the plugin has never been installed before. Use the
	 * "Install" operation instead to select a particular version of a plugin to install.
	 */
	UPDATE("Update"),

	/**
	 * Updates all plugins that are marked as having updates available.
	 */
	UPDATE_ALL("Update All"),

	/**
	 * Reset all plugin states to their original states, which means that this operation
	 * clears all scheduled operations.
	 */
	RESET("Reset"),

	/**
	 * Collapses all child version nodes in the manager.
	 */
	COLLAPSE_ALL("Collapse All");

	private String name;

	private PluginOperation(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
