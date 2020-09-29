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

import org.springframework.ide.eclipse.boot.dash.cf.client.CFServiceInstance;

public class CFServiceInstanceData implements CFServiceInstance {

	private String name;

	public CFServiceInstanceData(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPlan() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDashboardUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentationUrl() {
		// TODO Auto-generated method stub
		return null;
	}

}
