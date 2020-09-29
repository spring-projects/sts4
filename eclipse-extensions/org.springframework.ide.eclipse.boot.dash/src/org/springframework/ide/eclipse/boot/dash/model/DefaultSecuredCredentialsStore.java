/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;

/**
 * Secured storage for {@link RunTarget} passwords.
 *
 */
public class DefaultSecuredCredentialsStore implements SecuredCredentialsStore {

	private static final String KEY_PASSWORD = "password";

	private boolean isUnlocked = false;

	public DefaultSecuredCredentialsStore() {
	}

	public void remove(String runTargetId) {
		ISecurePreferences preferences = getSecurePreferences(runTargetId);
		if (preferences != null) {
			preferences.removeNode();
		}
	}

	@Override
	public String getCredentials(String runTargetId) throws StorageException {
		return readProperty(KEY_PASSWORD, runTargetId);
	}

	@Override
	public void setCredentials(String runTargetId, String password) throws StorageException {
		setProperty(KEY_PASSWORD, password, runTargetId);
	}

	private ISecurePreferences getSecurePreferences(String runTargetId) {
		ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault().node(BootDashActivator.PLUGIN_ID);
		securePreferences = securePreferences.node(EncodingUtils.encodeSlashes(runTargetId));
		return securePreferences;
	}

	private String readProperty(String property, String runTargetId) throws StorageException {
		ISecurePreferences preferences = getSecurePreferences(runTargetId);
		String val = null;
		if (preferences != null) {
			val = preferences.get(property, null);
			//We've succesfully used it so... it must be unlocked now.
			isUnlocked = true;
		}
		return val;
	}

	private void setProperty(String property, String value, String runTargetId) throws StorageException {
		ISecurePreferences preferences = getSecurePreferences(runTargetId);
		if (preferences != null) {
			if (value == null) {
				preferences.remove(property);
			} else {
				preferences.put(property, value, true);
			}
			//We've succesfully used it so... it must be unlocked now.
			isUnlocked = true;
		}
	}

	@Override
	public boolean isUnlocked() {
		return isUnlocked;
	}

}
