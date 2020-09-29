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
 * Basic implementation of a command parameter instance. It contains a reference
 * to the parameter descriptor.
 * @author Nieraj Singh
 */
public class CommandParameter implements ICommandParameter,
		IValueChangedNotifier {

	private Object value;
	private ICommandParameterDescriptor descriptor;
	private List<IParameterValueListener> listeners;

	public CommandParameter(ICommandParameterDescriptor descriptor) {
		this.descriptor = descriptor;
		init();
	}
	
	protected void init() {
		if (descriptor != null) {
			Object defaultValue =  descriptor.getDefaultValue();
			if (defaultValue != null) {
				// Internal set value. Do not fire events for this
				value = defaultValue;
			}
		}
	}

	public ICommandParameterDescriptor getParameterDescriptor() {
		return descriptor;
	}

	public void setValue(Object value) {
		this.value = value;
		notifyValueChanged(value);
	}

	public Object getValue() {
		return value;
	}

	public boolean hasValue() {
		return value != null;
	}

	public IParameterValueListener addListener(IParameterValueListener listener) {
		if (listener == null) {
			return null;
		}
		if (listeners != null) {
			listeners = new ArrayList<IParameterValueListener>();
		}

		if (listeners.contains(listener)) {
			return null;
		}

		listeners.add(listener);

		return listener;
	}

	public IParameterValueListener removeListener(
			IParameterValueListener listener) {
		if (listeners != null && listeners.remove(listener)) {
			return listener;
		}
		return null;
	}

	protected void notifyValueChanged(Object value) {
		if (listeners == null) {
			return;
		}

		ParameterValueEvent event = new ParameterValueEvent(value);
		for (IParameterValueListener listener : listeners) {
			listener.handleValueChanged(event);
		}
	}

}
