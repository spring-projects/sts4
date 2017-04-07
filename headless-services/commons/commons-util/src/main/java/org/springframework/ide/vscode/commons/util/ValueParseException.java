/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

/**
 * Exception if there is a failure when parsing a value. It does not wrap
 * other exceptions such that when thrown, the parse exception is the "deepest"
 * error.
 *
 */
public class ValueParseException extends Exception {

	private int startIndex = -1;
	private int endIndex = -1;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ValueParseException(String message) {
		super(message);
	}
	
	public ValueParseException(String message, int startIndex, int endIndex) {
		this(message);
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

}
