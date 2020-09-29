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

import java.util.Collections;
import java.util.List;

/**
 * @author Nieraj Singh
 */
public class CompositeParameterDescriptor extends ParameterDescriptor {

	private List<ICommandParameterDescriptor> descriptors;

	public CompositeParameterDescriptor(String name, String description,
			boolean isMandatory, Object defaultValue,
			boolean requiresNameInCommand, String argumentDelimiter,
			String valueSeparator, List<ICommandParameterDescriptor> descriptors) {
		super(name, description, isMandatory, defaultValue,
				ParameterKind.COMPOSITE, requiresNameInCommand,
				argumentDelimiter, valueSeparator);
		this.descriptors = descriptors;
	}

	/**
	 * Should never be null. Return empty if it contains no child descriptors
	 * 
	 * @return
	 */
	public List<ICommandParameterDescriptor> getParameterDescriptors() {
		return descriptors != null ? descriptors
				: (descriptors = Collections.EMPTY_LIST);
	}
}
