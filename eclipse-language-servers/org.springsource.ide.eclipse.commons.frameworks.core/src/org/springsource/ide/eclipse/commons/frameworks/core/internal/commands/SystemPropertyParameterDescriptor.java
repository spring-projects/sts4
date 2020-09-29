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
 * System property parameter descriptor that has "-D" prefix and a value separator "=".
 * It is  a base type text parameter, therefore has a free text field (no filters or content assist)
 * @author Nieraj Singh
 */
public class SystemPropertyParameterDescriptor extends ParameterDescriptor {

	public static final String SYSTEM_PREFIX = "-D";
	public static final String VALUE_SEPARATOR = "=";

	public SystemPropertyParameterDescriptor(String name, String description,
			boolean isMandatory, Object defaultValue) {
		super(name, description, isMandatory, defaultValue, ParameterKind.BASE,
				true, SYSTEM_PREFIX, VALUE_SEPARATOR);
	}

}
