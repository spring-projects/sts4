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

import org.springsource.ide.eclipse.commons.frameworks.ui.internal.icons.IIcon;

/**
 * Indicates the different states of a plugin in the plugin manager.
 * <p>
 * The ORDER in which these literals appears matters, as it affects sorting
 * order in the plugin manager. That is, states that are defined first have
 * higher priority in terms of sorting that those states that are listed last.
 * </p>
 * 
 * @author nisingh
 * 
 */
public enum PluginState implements IIcon {
	// Ordered in priority. Be careful when changing, as this affects sort
	// order in the plugin manager
	/**
	 * Plugin version that has been scheduled for Install OR Upgrade
	 */
	SELECT_INSTALL(
			"platform:/plugin/org.springsource.ide.eclipse.commons.frameworks.ui/icons/full/obj16/select_install.gif",
			"Plugin selected for install"),

	/**
	 * Plugin that has been scheduled for Uninstall
	 */
	SELECT_UNINSTALL(
			"platform:/plugin/org.springsource.ide.eclipse.commons.frameworks.ui/icons/full/obj16/select_uninstall.gif",
			"Plugin selected for uninstall"),
	/**
	 * Plugin that is currently installed in a given project and has no upgrades
	 * available, meaning that although newer milestone version of the plugin
	 * may exist, if the plugin is installed using the corresponding command
	 * without specifying a version, the version that is installed is considered
	 * the "latest version". Plugins installed with the "latest version" are
	 * marked with this state.
	 */
	INSTALLED(
			"platform:/plugin/org.springsource.ide.eclipse.commons.frameworks.ui/icons/full/obj16/installed.gif",
			"Latest version of the plugin is installed"),

	/**
	 * A plugin that IS installed in a given project, but has newer versions
	 * available.
	 */
	UPDATE_AVAILABLE(
			"platform:/plugin/org.springsource.ide.eclipse.commons.frameworks.ui/icons/full/obj16/update_available.gif",
			"A more recent version available for update"),
	/**
	 * A plugin that is incompatible with the current version of frameworks used
	 * by a project
	 */
	INCOMPATIBLE(
			"platform:/plugin/org.springsource.ide.eclipse.commons.frameworks.ui/icons/full/obj16/incompatible.gif",
			"Plugin is incompatible with the version of the runtime used by the project"), ;

	private String iconLocation;
	private String title;

	private PluginState(String iconLocation, String title) {
		this.iconLocation = iconLocation;
		this.title = title;
	}

	public String getIconLocation() {
		return iconLocation;
	}

	public String getTitle() {
		return title;
	}
}
