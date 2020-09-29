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
 * Describes command including its parameters. This is intended to be an IMMUTABLE class, and ideally can be used
 * globally to define a particular command.
 * <p>
 * To instantiate a command whose parameter values can be set, use
 * {@link IFrameworkCommand}</p>
 * @author Nieraj Singh
 */
public interface IFrameworkCommandDescriptor {
	/**
	 * Name of the command. Cannot be null. This is the command script name.
	 */
	public String getName();

	/**
	 * Description of the command. Optional.
	 */
	public String getDescription();
	
	/**
	 * List of parameter descriptors that describe the parameters of this command.
	 * <p>
	 * Note that this is order-sensitive, therefore the order in which they appear affects how
	 * the parameters are constructed in a command string.
	 * </p>
	 * @return
	 */
	public ICommandParameterDescriptor[] getParameters();
	
}
