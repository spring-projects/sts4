/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.commons.java;

public class JavaElementData {
	
	private String name;
	private String handleIdentifier;
	
	public JavaElementData() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHandleIdentifier() {
		return handleIdentifier;
	}

	public void setHandleIdentifier(String handleIdentifier) {
		this.handleIdentifier = handleIdentifier;
	}

}
