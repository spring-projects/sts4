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
 * Constructs command parameter descriptors, as well as command instances.
 * @author Nieraj Singh
 * @author Christian Dupuis
 */
public class CommandFactory {

	public static IFrameworkCommand createCommandInstance(IFrameworkCommandDescriptor descriptor) {
		if (descriptor == null) {
			return null;
		}
		return new FrameworkCommand(descriptor);
	}

	/*
	 * Command descriptor methods
	 */
	public static IFrameworkCommandDescriptor createCommandDescriptor(String name, String description,
			ICommandParameterDescriptor[] parameters) {
		return new FrameworkCommandDescriptor(name, description, parameters);
	}

}
