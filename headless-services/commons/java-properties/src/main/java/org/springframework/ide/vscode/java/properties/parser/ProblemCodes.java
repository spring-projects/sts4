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
 * Parsing error codes
 * 
 * @author Alex Boyko
 *
 */
public class ProblemCodes {
	public static final String PROPERTIES_SYNTAX_ERROR = "PROPERTIES_SYNTAX_ERROR";
	public static final String PROPERTIES_AMBIGUITY_ERROR = "AMBIGUITY_ERROR";
	public static final String PROPERTIES_FULL_CONTEXT_ERROR = "FULL_CONTEXT_ERROR";
	public static final String PROPERTIES_CONTEXT_SENSITIVITY_ERROR = "CONTEXT_SENSITIVITY_ERROR";	
}
