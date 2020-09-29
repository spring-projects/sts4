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

import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.PluginVersion;

/**
 * Basic implementation of a tree element, with additional functionality that
 * subclasses must implement like handling different tree element operations
 * (Install, Uninstall, etc..)
 * @author Nieraj Singh
 */
public abstract class AbstractTreeElement implements ITreeElement {
	private PluginVersion version;
	private PluginState state;

	public AbstractTreeElement(PluginVersion version) {
		this.version = version;
	}

	public PluginVersion getVersionModel() {
		return version;
	}

	public void setState(PluginState state) {
		this.state = state;
	}

	public PluginState getPluginState() {
		return this.state;
	}

	public boolean isValidOperation(PluginOperation type) {
		if (type == null) {
			return false;
		}
		boolean isValid = false;
		PluginState currentState = getPluginState();
		switch (type) {
		case INSTALL:
			isValid = canHandleInstall(currentState);
			break;
		case UNINSTALL:
			isValid = canHandleUninstall(currentState);
			break;
		case UPDATE:
			isValid = canHandleUpdate(currentState);
			break;
		}
		return isValid;
	}

	public void performOperation(PluginOperation type) {
		
		// Do not proceed if it cannot be performed
		if (!isValidOperation(type)) {
			return;
		}
		
		PluginState currentState = getPluginState();
		switch (type) {
		case INSTALL:
			handleInstall(currentState);
			break;
		case UNINSTALL:
			handleUninstall(currentState);
			break;
		case UPDATE:
			handleUpdate(currentState);
			break;
		}
	}

	/**
	 * Answers true if the tree element can be installed given the current state
	 * of the tree element which is passed as an argument Answers false
	 * otherwise
	 * 
	 * @param state
	 *           current state of the tree element
	 * @return true if it can be installed based on the current state. False
	 *        otherwise
	 */
	abstract protected boolean canHandleInstall(PluginState state);

	/**
	 * Answers true if the tree element can be uninstalled given the current
	 * state of the tree element which is passed as an argument Answers false
	 * otherwise
	 * 
	 * @param state
	 *           current state of the tree element
	 * @return true if it can be uninstalled based on the current state. False
	 *        otherwise
	 */
	abstract protected boolean canHandleUninstall(PluginState state);

	/**
	 * Answers true if the tree element can be updated given the current state
	 * of the tree element which is passed as an argument Answers false
	 * otherwise
	 * 
	 * @param state
	 *           current state of the tree element
	 * @return true if it can be updated based on the current state. False
	 *        otherwise
	 */
	abstract protected boolean canHandleUpdate(PluginState state);

	/**
	 * Perform the install operation on the tree element. This only gets invoked
	 * if the corresponding canHandleInstall method returns true.
	 * 
	 * @param state
	 *           current state of the tree element
	 */
	abstract protected void handleInstall(PluginState state);

	/**
	 * Perform the uninstall operation on the tree element. This only gets
	 * invoked if the corresponding canHandleUninstall method returns true.
	 * 
	 * @param state
	 *           current state of the tree element
	 */
	abstract protected void handleUninstall(PluginState state);

	/**
	 * Perform the update operation on the tree element. This only gets invoked
	 * if the corresponding canHandleUpdate method returns true
	 * 
	 * @param state
	 *           current state of the tree element
	 */
	abstract protected void handleUpdate(PluginState state);
}
