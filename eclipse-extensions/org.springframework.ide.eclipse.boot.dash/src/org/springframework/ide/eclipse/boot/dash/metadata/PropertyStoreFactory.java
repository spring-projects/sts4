/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.metadata;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.api.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.DefaultSecuredCredentialsStore;
import org.springframework.ide.eclipse.boot.dash.model.SecuredCredentialsStore;
import org.springsource.ide.eclipse.commons.core.pstore.IScopedPropertyStore;
import org.springsource.ide.eclipse.commons.core.pstore.PreferenceBasedStore;

public class PropertyStoreFactory {

	public static SecuredCredentialsStore createSecuredCredentialsStore() {
		return new DefaultSecuredCredentialsStore();
	}

	public static IScopedPropertyStore<RunTargetType> createForRunTargets() {
		return new PreferenceBasedStore<RunTargetType>() {
			protected IEclipsePreferences createPrefs(RunTargetType runTargetType) {
				return InstanceScope.INSTANCE.getNode(BootDashActivator.PLUGIN_ID + ':' + runTargetType.getName());
			}
		};
	}
}
