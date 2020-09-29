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
 * This describes a parameter descriptor, and is intended to be IMMUTABLE.
 * <p>
 * To instantiate a parameter whose values can be set, see
 * {@link ICommandParameter}</p>
 * Example of how to define a parameter descriptor:
 * <p>
 * -Dsys.env=prod
 * </p>
 * <p>
 * Here, "-D" is the prefix, "sys.env" the parameter name, "=" the value
 * separator, and "prod" a default value
 * </p>
 * @author Nieraj Singh
 */
public interface ICommandParameterDescriptor {

	/**
	 * This is the name of the parameter as it would appear in the actual
	 * command line String. It must be exactly as it would be used if a user
	 * would type it when executing a command in a command line.
	 * 
	 * @return name of the parameter. It's required
	 */
	public String getName();

	/**
	 * 
	 * @return optional description of the parameter that may include
	 *        information on what it does, and what values may be acceptable
	 */
	public String getDescription();

	/**
	 * 
	 * @return true if it requires a set value, false if it is an optional
	 *        parameter
	 */
	public boolean isMandatory();

	/**
	 * Identifier indicating the type of parameter. Must not be null.
	 * 
	 * @return non-null indentifier.
	 */
	public ParameterKind getParameterKind();

	/**
	 * Optionally, a parameter can specify a default value. Some parameters may
	 * always have a default value, like a boolean parameter, in which case the
	 * default is false. The value of the default is not expected to change
	 * throughout the life of the parameter, unlike the actual value.
	 * 
	 * @return a default value, or null if it has no default value. Empty
	 *        strings are considered default values, so only use null to
	 *        indicate no default value
	 */
	public Object getDefaultValue();

	/**
	 * If the parameter name needs to be included in the executable command
	 * string and must be prefixed by a delimiter like "--", return a non-null
	 * value. Return null if parameter name should not be prefixed, regardless
	 * of whether the parameter name itself should appear or not.
	 * 
	 * @return non-null String if it has a delimiter, null if not.
	 */
	public String getParameterPrefix();

	/**
	 * @return true if the parameter name forms part of the executable String,
	 *        or false if it should be omitted.
	 */
	public boolean requiresParameterNameInCommand();

	/**
	 * Optional. If a parameter requires a value separator that separates the
	 * value from the parameter name, like for instance '=' or ':', the
	 * parameter descriptor must return a non-null, non-empty separator. A null
	 * or empty separator will be ignored.
	 * <p>
	 * -Dsys.env=prod
	 * </p>
	 * <p>
	 * Here, "-D" is the prefix, "sys.env" the parameter name, and "=" the value
	 * separator
	 * </p>
	 * 
	 * @return the value separator
	 */
	public String getValueSeparator();

}
