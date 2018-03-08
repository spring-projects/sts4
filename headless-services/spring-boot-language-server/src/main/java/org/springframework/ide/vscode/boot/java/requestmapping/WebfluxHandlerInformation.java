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
	
	private final String handlerClass;
	private final String handlerMethod;
	
	private final String path;
	private final String httpMethod;
	private final String contentType;
	private final String acceptType;
	
	public WebfluxHandlerInformation(String handlerClass, String handlerMethod, String path, String httpMethod, String contentType, String acceptType) {
		this.handlerClass = handlerClass;
		this.handlerMethod = handlerMethod;
		
		this.path = path;
		this.httpMethod = httpMethod;
		this.contentType = contentType;
		this.acceptType = acceptType;
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
	
	public String getHttpMethod() {
		return httpMethod;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public String getAcceptType() {
		return acceptType;
	}

}
