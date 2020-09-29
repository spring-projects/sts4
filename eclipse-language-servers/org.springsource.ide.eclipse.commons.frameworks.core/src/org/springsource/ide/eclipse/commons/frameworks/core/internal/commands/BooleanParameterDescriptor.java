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
 * @author Nieraj Singh
 */
public class BooleanParameterDescriptor extends ParameterDescriptor {

	private boolean useNameAsValue;

	/**
	 * Use this constructor if the boolean value is either "true" or "false", meaning that the parameter has a
	 * pattern:
	 * <p>
	 * [parametername]=true
	 * </p>
	 * <p>
	 * [parametername]=false
	 * </p>
	 * 
	 * @param name
	 * @param description
	 * @param isMandatory
	 * @param defaultValue
	 * @param requiresNameInCommand
	 * @param parameterPrefix
	 * @param valueSeparator
	 */
	public BooleanParameterDescriptor(String name, String description,
			boolean isMandatory, boolean defaultValue,
			boolean requiresNameInCommand, String parameterPrefix,
			String valueSeparator) {
		super(name, description, isMandatory, new Boolean(defaultValue),
				ParameterKind.BOOLEAN, requiresNameInCommand, parameterPrefix,
				valueSeparator);
		useNameAsValue = false;
	}

	/**
	 * User this constructor if the name of the parameter itself is also the
	 * value. This should be used when the parameter either appears in the command
	 * string with its name if set to true, or is omitted altogether if the value is false
	 * 
	 * 
	 * @param name
	 * @param description
	 * @param isMandatory
	 * @param defaultValue
	 * @param parameterPrefix
	 */
	public BooleanParameterDescriptor(String name, String description,
			boolean isMandatory, boolean defaultValue, String parameterPrefix) {
		super(name, description, isMandatory, new Boolean(defaultValue),
				ParameterKind.BOOLEAN, true, parameterPrefix, null);
		useNameAsValue = true;
	}

	/**
	 * If true, the name of the parameter should be used as a value, instead of
	 * using "true" or "false". Therefore , in this case, the if the boolean is
	 * true, the name of the parameter is included, if boolean is false, the
	 * parameter is omitted.
	 * 
	 * @return
	 */
	public boolean useNameAsValue() {
		return useNameAsValue;
	}

}
