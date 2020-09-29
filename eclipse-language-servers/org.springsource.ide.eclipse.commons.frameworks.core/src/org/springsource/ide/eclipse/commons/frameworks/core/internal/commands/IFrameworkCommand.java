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

import java.util.List;

/**
 * Represents a command instance with a list of ordered parameters. Implementors
 * can have the command instance generate parameter instances for the parameter
 * descriptors contained in the associated command descriptor.
 * <p>
 * Each instance is associated with one command descriptor, and it must hold a
 * reference to the command descriptor it is representing
 * </p>
 * @author Nieraj Singh
 */
public interface IFrameworkCommand {

	/**
	 * Associated command descriptor. Must not be null.
	 * 
	 * @return
	 */
	public IFrameworkCommandDescriptor getCommandDescriptor();

	/**
	 * List of parameters for this command. Note that order matters, therefore
	 * the order in which the parameters appear should match the order in which
	 * they are used as arguments to the command. This is a MUTABLE list, so
	 * callers can add or remove parameter instances, although the corresponding
	 * parameter descriptors cannot be modified
	 * 
	 * @return ordered list of command parameters. May be empty but never null
	 */
	public List<ICommandParameter> getParameters();

}
