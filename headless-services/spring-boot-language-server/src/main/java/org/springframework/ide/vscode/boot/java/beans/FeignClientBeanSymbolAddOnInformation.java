/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans;

public class FeignClientBeanSymbolAddOnInformation extends BeansSymbolAddOnInformation {
	
	final public String[] configClasses;

	public FeignClientBeanSymbolAddOnInformation(String beanID, String beanType, String... configClasses) {
		super(beanID, beanType);
		this.configClasses = configClasses;
	}

}
