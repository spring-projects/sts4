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
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import java.util.UUID;

import org.springframework.ide.eclipse.boot.dash.cf.client.CFOrganization;

/**
 * Data object implementing {@link CFOrganization}
 *
 * @author Kris De Volder
 */
public class CFOrganizationData implements CFOrganization {

	private String name;
	private UUID guid;

	public CFOrganizationData(String name, UUID guid) {
		super();
		this.name = name;
		this.guid = guid;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public UUID getGuid() {
		return guid;
	}

}
