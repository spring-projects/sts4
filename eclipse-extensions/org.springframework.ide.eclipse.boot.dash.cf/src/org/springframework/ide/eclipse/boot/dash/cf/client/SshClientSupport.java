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
package org.springframework.ide.eclipse.boot.dash.cf.client;

import java.util.UUID;

public interface SshClientSupport {

	SshHost getSshHost() throws Exception;
	String getSshUser(String appName, int instance) throws Exception;
	String getSshCode() throws Exception;

	/**
	 * Deprecated because it is not supported with V2. Use the method based on appName instead.
	 */
	@Deprecated
	String getSshUser(UUID appGuid, int instance) throws Exception;

}
