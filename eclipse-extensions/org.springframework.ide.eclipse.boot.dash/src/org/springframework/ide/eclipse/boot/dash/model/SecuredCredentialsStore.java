/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import org.eclipse.equinox.security.storage.StorageException;

public interface SecuredCredentialsStore {

	String getCredentials(String string) throws StorageException;
	void setCredentials(String runTargetId, String password) throws StorageException;
	boolean isUnlocked();

}
