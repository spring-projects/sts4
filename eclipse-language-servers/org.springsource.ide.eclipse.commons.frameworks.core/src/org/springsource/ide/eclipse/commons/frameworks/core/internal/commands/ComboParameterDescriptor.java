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
 * A combo parameter must specify an ordered list of valid values. If also
 * specifying a default value, the value should be part of the list of valid
 * values.
 * @author Nieraj Singh
 */
public class ComboParameterDescriptor extends ParameterDescriptor {
	private String[] values;

	public ComboParameterDescriptor(String name, String description,
			boolean isMandatory, Object defaultValue, boolean requiresName,
			String delimiter, String valueSeparator, String[] values) {
		super(name, description, isMandatory, defaultValue,
				ParameterKind.COMBO, requiresName, delimiter, valueSeparator);
		this.values = values;
	}

	public String[] getSelectionValues() {
		return values;
	}
}
