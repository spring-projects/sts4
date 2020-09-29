/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.internal.ui;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Add YAML files to AnyEditTools preferences exclusion list by default.
 *
 * TODO: Remove once AnyEditTools 2.6.2 or higher is available
 *
 * @author Alex Boyko
 *
 */
public class AnyEditToolsPrefsInitializer implements IStartup {

	private static final String ANY_EDIT__BUNDLE_NAME = "de.loskutov.anyedit.AnyEditTools";

    private static final String ANY_EDIT__PREF_ACTIVE_FILTERS_LIST = "activeContentFilterList";

    private static final String YAML_FILTER = "*.yml";

	@Override
	public void earlyStartup() {
		initAnyEditPreferences();
	}

	private static void initAnyEditPreferences() {
		if (Platform.getBundle(ANY_EDIT__BUNDLE_NAME) != null) {
			ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, ANY_EDIT__BUNDLE_NAME);
			StringBuilder defaultExclusionList = new StringBuilder(store.getDefaultString(ANY_EDIT__PREF_ACTIVE_FILTERS_LIST));
			if (defaultExclusionList.indexOf(YAML_FILTER) == -1) {
				store.setDefault(ANY_EDIT__PREF_ACTIVE_FILTERS_LIST, defaultExclusionList.append(",").append(YAML_FILTER).toString());
			}
		}
	}

}
