/*******************************************************************************
 * Copyright (c) 2025 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

public class WebfluxHandlerMethodIndexElement extends RequestMappingIndexElement {

	private final String handlerClass;
	private final String handlerMethod;
	
	public WebfluxHandlerMethodIndexElement(String handlerClass, String handlerMethod, String path, String[] httpMethods, String[] contentTypes, String[] acceptTypes) {
		super(path, httpMethods, contentTypes, acceptTypes);

		this.handlerClass = handlerClass;
		this.handlerMethod = handlerMethod;
	}
	
	public String getHandlerClass() {
		return handlerClass;
	}
	
	public String getHandlerMethod() {
		return handlerMethod;
	}
	
}
