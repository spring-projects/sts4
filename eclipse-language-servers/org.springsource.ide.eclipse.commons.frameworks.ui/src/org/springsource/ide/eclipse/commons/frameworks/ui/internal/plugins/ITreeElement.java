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
 * Base type that appears in the tree viewer. Represents a plugin entity with a
 * viewer state that indicates the state of the plugin as it appears in the
 * dialogue tree viewer (i.e whether the plugin is marked for install,
 * uninstall, has update available, etc...)
 * <p>
 * Every tree element must correspond to an underlying plugin model version entity.
 * </p>
 * This object is intended to be mutable, except for its reference to the
 * underlying plugin model version. The latter should be immutable.
 * @author Nieraj Singh
 */
public interface ITreeElement {
	/**
	 * Return the plugin version in the plugin model associated with this tree
	 * element. Every tree element must have a plugin version associated with
	 * it. This cannot be null.
	 * 
	 * @return version model associated with the tree element. Must not be null
	 */
	public PluginVersion getVersionModel();

	/**
	 * State of plugin. Can be changed during the lifespan of the plugin
	 * manager. Can be set to null, indicating that it has no state.
	 * 
	 * @param state
	 */
	public void setState(PluginState state);

	/**
	 * 
	 * @return the current state of the element. Can be null. If null, it has no
	 *        state. 
	 */
	public PluginState getPluginState();

	/**
	 * Answers whether a particular operation is valid on this element based on
	 * the element's current state.
	 * 
	 * @param type
	 *           operation type
	 * @return true if the operation is valid for the element's current state.
	 */
	public boolean isValidOperation(PluginOperation type);

	/**
	 * Peforms an operation IF the operation is valid for the given tree element
	 * and tree element state
	 * 
	 * @param type
	 */
	public void performOperation(PluginOperation type);
}
