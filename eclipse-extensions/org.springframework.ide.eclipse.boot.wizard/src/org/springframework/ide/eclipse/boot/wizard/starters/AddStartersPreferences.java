/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import org.eclipse.jface.preference.IPreferenceStore;
import org.springframework.ide.eclipse.boot.core.BootPreferences;

/**
 * Wrapper around preference store that is injected into the Add Starters wizard model.
 *
 * The wrapper allows mocking for testing.
 *
 */
public class AddStartersPreferences {

	private final IPreferenceStore preferenceStore;

	public AddStartersPreferences(IPreferenceStore preferenceStore) {
		this.preferenceStore = preferenceStore;
	}

	public IPreferenceStore getPreferenceStore() {
		return preferenceStore;
	}

	public String getInitializrUrl() {
		return BootPreferences.getInitializrUrl();
	}

	public String[] getInitializrUrls() {
		return BootPreferences.getInitializrUrls();
	}

	public void addInitializrUrl(String url) {
		BootPreferences.addInitializrUrl(url);
	}

}
