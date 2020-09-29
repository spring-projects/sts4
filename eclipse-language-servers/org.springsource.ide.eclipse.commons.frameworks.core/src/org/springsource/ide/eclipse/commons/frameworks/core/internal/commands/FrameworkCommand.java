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

import java.util.ArrayList;
import java.util.List;

/**
 * Basic framework command implementation. Represents an INSTANCE of a command.
 * <p/>
 * The command description and name are immutable, but the list of parameters is
 * mutable.
 * @author Nieraj Singh
 */
public class FrameworkCommand implements IFrameworkCommand {

	private IFrameworkCommandDescriptor descriptor;
	private List<ICommandParameter> parameters;

	public FrameworkCommand(IFrameworkCommandDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	public List<ICommandParameter> getParameters() {
		if (parameters == null) {
			this.parameters = new ArrayList<ICommandParameter>();
			ICommandParameterDescriptor[] parameterDescriptors = descriptor
					.getParameters();
			for (ICommandParameterDescriptor paramDescriptor : parameterDescriptors) {
				if (paramDescriptor != null) {
					ICommandParameter parameter = getCommandParameter(paramDescriptor);
					if (parameter != null) {
						parameters.add(parameter);
					}
				}
			}
		}
		return parameters;
	}

	protected ICommandParameter getCommandParameter(
			ICommandParameterDescriptor descriptor) {
		return ParameterFactory.getParameterInstance(descriptor);
	}

	public IFrameworkCommandDescriptor getCommandDescriptor() {
		return descriptor;
	}

}
