/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.java.properties.parser;

/**
 * Parsing problem interface
 * 
 * @author Alex Boyko
 *
 */
public interface Problem {
	
	/**
	 * The parsing problem message
	 * @return The message string
	 */
	String getMessage();
	
	/**
	 * Problem's code
	 * @return The code
	 */
	String getCode();
	
	/**
	 * Problem's start index in the document
	 * @return Start index relative to the document
	 */
	int getOffset();
	
	/**
	 * Problem's range in symbols 
	 * @return Number of characters
	 */
	int getLength();
	
}
