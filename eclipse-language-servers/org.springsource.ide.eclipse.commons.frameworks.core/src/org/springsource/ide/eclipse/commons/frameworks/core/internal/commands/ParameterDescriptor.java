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

import org.eclipse.core.runtime.PlatformObject;

/**
 * Base implementation of a parameter descriptor.
 * @author Nieraj Singh
 */
public class ParameterDescriptor extends PlatformObject implements
		ICommandParameterDescriptor {

	private String name;
	private String description;
	private boolean isMandatory;
	private Object defaultValue;
	private ParameterKind kind;
	private boolean requiresNameInCommand = false;
	private String parameterPrefix;
	private String valueSeparator;

	public ParameterDescriptor(String name, String description,
			boolean isMandatory, Object defaultValue, ParameterKind kind,
			boolean requiresNameInCommand, String parameterPrefix,
			String valueSeparator) {
		super();
		this.name = name;
		this.description = description;
		this.isMandatory = isMandatory;
		this.defaultValue = defaultValue;
		this.kind = kind;
		this.requiresNameInCommand = requiresNameInCommand;
		this.parameterPrefix = parameterPrefix;
		this.valueSeparator = valueSeparator;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isMandatory() {
		return isMandatory;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public ParameterKind getParameterKind() {
		return kind;
	}

	public String getParameterPrefix() {
		return parameterPrefix;
	}

	public String getValueSeparator() {
		return valueSeparator;
	}

	public boolean requiresParameterNameInCommand() {
		return requiresNameInCommand;
	}

	public Object getAdapter(Class adapter) {
		// Ask the adapter manager first
		Object adapterObj = super.getAdapter(adapter);

		// If no adapter can be found, use the default factory
		if (adapterObj == null && adapter == IParameterFactory.class) {
			adapterObj = new ParameterFactory();
		}
		return adapterObj;
	}
}
