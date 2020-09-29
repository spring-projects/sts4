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
package org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins;

/**
 * Model element that represents a version of a plugin. It is usually a
 * child of a plugin model element.
 * 
 * @author Nieraj Singh
 * @author Kris De Volder
 */
public class PluginVersion extends BasePluginData {

	private Plugin parent;
	private boolean isInstalled;

	public PluginVersion() {
		//
	}

	public PluginVersion(BasePluginData data) {
		setData(data);
	}

	public void setParent(Plugin parent) {
		this.parent = parent;
	}

	public Plugin getParent() {
		return parent;
	}

	public void setInstalled(boolean isInstalled) {
		this.isInstalled = isInstalled;
	}

	public boolean isInstalled() {
		return isInstalled;
	}

	/**
	 * Support for copying a dependency data into the version model.
	 * 
	 * @param data
	 */
	protected void setData(BasePluginData data) {
		if (data == null) {
			return;
		}
		setName(data.getName());
		setAuthor(data.getAuthor());
		setDescription(data.getDescription());
		setDocumentation(data.getDocumentation());
		setRuntimeVersion(data.getRuntimeVersion());
		setVersion(data.getVersion());
		setTitle(data.getTitle());
	}
	
	@Override
	public String toString() {
		return getName()+"["+getVersion()+"]";
	}

}
