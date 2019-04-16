/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans;

import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;

/**
 * @author Martin Lippert
 */
public class BeansSymbolAddOnInformation implements SymbolAddOnInformation {

	private final String beanID;

	public BeansSymbolAddOnInformation(String beanID) {
		this.beanID = beanID;
	}

	public String getBeanID() {
		return beanID;
	}

}
