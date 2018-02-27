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
package org.springframework.ide.vscode.boot.java.requestmapping;

/**
 * @author Martin Lippert
 */
public class WebfluxHandlerInformation {
	
	private final String symbol;
	private String destinationClass;
	private String methodKey;
	
	public WebfluxHandlerInformation(String symbol, String destinationClass, String methodKey) {
		this.symbol = symbol;
		this.destinationClass = destinationClass;
		this.methodKey = methodKey;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public String getDestinationClass() {
		return destinationClass;
	}
	
	public String getMethodKey() {
		return methodKey;
	}

}
