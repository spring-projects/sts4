/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.internal.commands;

/**
 * Represents the different types of parameters. 
 * @author nisingh
 *
 */
public enum ParameterKind {
	
	// Parameter with a String value
	BASE,
	
	// Parameter with a set of defined values
	COMBO,
	
	// Parameter with a Java type value
	JAVA_TYPE,
	
	// Boolean parameter
	BOOLEAN, 
	
	// A composite parameter
	COMPOSITE
	

}
