/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.client;

import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFDomainStatus;

public interface CFCloudDomain {
	String getName();
	CFDomainType getType();
	CFDomainStatus getStatus();

	/**
	 * If the given hostAndDomain is of the form ${host}.${domain} then
	 * return the host part. Otherwise return null.
	 */
	default String splitHost(String hostAndDomain) {
		String name = getName();
		if (hostAndDomain.endsWith("."+name)) {
			return hostAndDomain.substring(0, hostAndDomain.length()-name.length()-1);
		}
		return null;
	}
}
