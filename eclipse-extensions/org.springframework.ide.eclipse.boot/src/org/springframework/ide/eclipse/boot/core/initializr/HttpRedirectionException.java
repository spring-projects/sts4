/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.initializr;

public class HttpRedirectionException extends Exception {

	private static final long serialVersionUID = 1L;
	public final String redirectedTo;

	public HttpRedirectionException(String redirectedTo) {
		this.redirectedTo = redirectedTo;
	}

	@Override
	public String toString() {
		return "HttpRedirectionException("+redirectedTo+")";
	}
}
