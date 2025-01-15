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
package org.springframework.ide.vscode.boot.java.requestmapping;

import org.springframework.ide.vscode.commons.protocol.spring.AbstractSpringIndexElement;

public class WebfluxHandlerMethodIndexElement extends AbstractSpringIndexElement {

	private final String handlerClass;
	private final String handlerMethod;
	
	private final String path;
	private final String[] httpMethods;
	private final String[] contentTypes;
	private final String[] acceptTypes;
	
	public WebfluxHandlerMethodIndexElement(String handlerClass, String handlerMethod, String path, String[] httpMethods, String[] contentTypes, String[] acceptTypes) {
		super(AbstractSpringIndexElement.NO_CHILDREN);
		
		this.handlerClass = handlerClass;
		this.handlerMethod = handlerMethod;
		
		this.path = path;
		this.httpMethods = httpMethods;
		this.contentTypes = contentTypes;
		this.acceptTypes = acceptTypes;
	}
	
	public String getHandlerClass() {
		return handlerClass;
	}
	
	public String getHandlerMethod() {
		return handlerMethod;
	}
	
	public String getPath() {
		return path;
	}
	
	public String[] getHttpMethods() {
		return httpMethods;
	}
	
	public String[] getContentTypes() {
		return contentTypes;
	}
	
	public String[] getAcceptTypes() {
		return acceptTypes;
	}

}
