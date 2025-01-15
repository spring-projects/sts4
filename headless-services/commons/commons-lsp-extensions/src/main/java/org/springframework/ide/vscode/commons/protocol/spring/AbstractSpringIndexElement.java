/*******************************************************************************
 * Copyright (c) 2024 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.spring;

public abstract class AbstractSpringIndexElement implements SpringIndexElement {
	
	public static final SpringIndexElement[] NO_CHILDREN = new SpringIndexElement[0];
	
	private final SpringIndexElement[] children;

	public AbstractSpringIndexElement(SpringIndexElement[] children) {
		this.children = children != null ? children : NO_CHILDREN;
	}

	@Override
	public SpringIndexElement[] getChildren() {
		return children;
	}

}
