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
 * A String command parameter where a null OR empty String means there is no value.
 * @author Nieraj Singh
 */
public class StringCommandParameter extends CommandParameter {

	public StringCommandParameter(ICommandParameterDescriptor descriptor) {
		super(descriptor);
	}

	public boolean hasValue() {
		boolean hasVal = super.hasValue();
		Object value = getValue();
		if (hasVal && value instanceof String) {
			hasVal &= ((String) value).length() > 0;
		}
		return hasVal;
	}

}
