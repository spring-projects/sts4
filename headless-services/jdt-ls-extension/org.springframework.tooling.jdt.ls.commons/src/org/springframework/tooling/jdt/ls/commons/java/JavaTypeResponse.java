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

public class JavaTypeResponse {

	private TypeData data;

	public JavaTypeResponse(TypeData data) {
		this.data = data;
	}

	public TypeData getData() {
		return data;
	}

	public void setData(TypeData data) {
		this.data = data;
	}

}
