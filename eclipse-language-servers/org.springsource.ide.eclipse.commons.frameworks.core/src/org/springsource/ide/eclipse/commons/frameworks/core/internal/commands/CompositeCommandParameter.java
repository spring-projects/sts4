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
 * @author Nieraj Singh
 */
public class CompositeCommandParameter extends CommandParameter {

	private List<ICommandParameter> parameters;

	public CompositeCommandParameter(CompositeParameterDescriptor descriptor) {
		super(descriptor);
	}

	/**
	 * Never null.
	 * 
	 * @return
	 */
	public List<ICommandParameter> getParameters() {
		if (parameters == null) {
			parameters = new ArrayList<ICommandParameter>();
			CompositeParameterDescriptor descriptor = (CompositeParameterDescriptor) getParameterDescriptor();
			List<ICommandParameterDescriptor> childDescriptors = descriptor
					.getParameterDescriptors();
			for (ICommandParameterDescriptor child : childDescriptors) {
				ICommandParameter parameter = ParameterFactory
						.getParameterInstance(child);
				if (parameter != null) {
					parameters.add(parameter);
				}
			}
		}
		return parameters;
	}

	/**
	 * For a composite parameter, the value of the parameter is the value of one
	 * of it's child parameters whose value has been set. If none of the child
	 * parameters have value set, null is returned.
	 */
	public Object getValue() {
		ICommandParameter setChildParameter = getSetChildCommandParameter();
		if (setChildParameter != null) {
			return setChildParameter.getValue();
		}
		return null;
	}

	/**
	 * If a composite parameter has a child parameter with a value set, return
	 * true. If none of the composite parameter's children have value set,
	 * return false.
	 */
	public boolean hasValue() {
		return getValue() != null;
	}

	/**
	 * Returns the child command parameter that has a set value. Ideally there
	 * should only be at most one command parameter with a value, but if more
	 * than one is encountered, the first one is returned.
	 * 
	 * @return first command parameter with value set that is found, or null if
	 *        none of the child parameters have value.
	 */
	public ICommandParameter getSetChildCommandParameter() {
		if (parameters == null || parameters.isEmpty()) {
			return null;
		}

		for (ICommandParameter parameter : parameters) {
			// There should at most only be one parameter with a value
			// If not, return the first one encountered
			if (parameter.hasValue()) {
				return parameter;
			}
		}
		return null;
	}
}
