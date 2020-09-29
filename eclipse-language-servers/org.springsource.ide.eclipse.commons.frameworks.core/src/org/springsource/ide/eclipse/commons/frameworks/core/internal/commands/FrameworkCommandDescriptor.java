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
 * Basic implementation of a command descriptor. The list of parameters should not be null, although
 * it can be empty.
 * @author Nieraj Singh
 */
public class FrameworkCommandDescriptor implements IFrameworkCommandDescriptor {
	private String name;
	private String description;
	private ICommandParameterDescriptor[] parameters;

	public FrameworkCommandDescriptor(String name, String description,
			ICommandParameterDescriptor[] parameters) {
		this.name = name;
		this.description = description;
		this.parameters = parameters != null ? parameters
				: new ICommandParameterDescriptor[] {};
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * @see com.springsource.sts.frameworks.core.internal.commands.IFrameworkCommandDescriptor#getParameters()
	 */
	public ICommandParameterDescriptor[] getParameters() {
		return parameters;
	}
}
