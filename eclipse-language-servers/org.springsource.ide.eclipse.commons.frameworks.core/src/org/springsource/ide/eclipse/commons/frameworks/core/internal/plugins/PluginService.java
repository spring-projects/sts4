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

import java.util.List;

/**
 * An abstract implementation of a service for managing plug-ins.
 * 
 * @author Steffen Pingel
 */
public abstract class PluginService {

	public enum InstallOrUpgradeStatus {
		SUCCESS, FAILED, INVALID_REPOSITORY_URL, VERIFICATION_NEEDED
	}
	
	public abstract InstallOrUpgradeStatus install(PluginVersion plugin);
	
	public abstract List<Plugin> search(String searchTerms, boolean refresh,
			boolean trustedOnly, boolean compatibleOnly);
	
	public abstract InstallOrUpgradeStatus remove(PluginVersion plugin);
	
	public abstract InstallOrUpgradeStatus upgrade(PluginVersion plugin);

}
