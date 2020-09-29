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

import org.eclipse.jdt.core.IJavaElement;

/**
 * Java parameter, where a java element type must be specified and may be used for content assist and other
 * operations that require a Java type.
 * <p/>
 * The default value can be a qualified Java name, although it is optional.
 * @author Nieraj Singh
 */
public class JavaParameterDescriptor extends ParameterDescriptor {
	
	/**
	 * List of valid Java types.
	 */
	public static final int FLAG_INTERFACE = 1 << 2;

	public static final int FLAG_CLASS = 1 << 3;

	public static final int FLAG_PACKAGE = 1 << 4;
	
	private int type;
	
	/**
	 * 
	 * @param name
	 *           of the parameter. required
	 * @param description
	 *           description is optional.
	 * @param isMandatory
	 *           if value is needed, set to true, false otherwise
	 * @param defaultValue
	 * @param type
	 *           a IJavaElement type identifier, that indicates whether
	 *           possible content assist should be for packages, types, etc..
	 */
	public JavaParameterDescriptor(String name, String description,
			boolean isMandatory, Object defaultValue, ParameterKind kind,
			int type, boolean requiresName, String delimiter, String valueSeparator) {
		super(name, description, isMandatory, defaultValue,
				ParameterKind.JAVA_TYPE, requiresName, delimiter, valueSeparator);
		this.type = type;
	}

	/**
	 * Get the java element type for this parameter. This may be used for
	 * content assist to decide what type of java element types to search for.
	 * 
	 * @see IJavaElement
	 * @return java element type.
	 */
	public int getJavaElementType() {
		return type;
	}
}
