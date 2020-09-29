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

import org.springframework.ide.eclipse.boot.dash.cf.client.CFStack;

/**
 * @author Kris De Volder
 */
public class MockCFStack implements CFStack {

	private String name;

	public MockCFStack(String name) {
		super();
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
