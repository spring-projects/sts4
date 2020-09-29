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
 * A command parameter instance that contains actual parameter values that can be run in a command
 * <p>
 * The command parameter contains a reference to a parameter descriptor that describes the parameter, like its name,
 * description, type, and default value.
 * </p>
 * @author Nieraj Singh
 */
public interface ICommandParameter {

	/**
	 * Get the parameter descriptor for this parameter instance
	 * @return
	 */
	public ICommandParameterDescriptor getParameterDescriptor();

	/**
	 * Set the actual value of the parameter. Usually set by the UI. Note that
	 * this should be stored separately from the default value, even if both are
	 * the same.
	 * 
	 * @param actual
	 *           parameter value.
	 */
	public void setValue(Object value);

	/**
	 * @return the actual parameter value. This is always a separate value than
	 *        the default
	 */
	public Object getValue();

	/**
	 * 
	 * @return True if it has a value, false otherwise
	 */
	public boolean hasValue();

}
