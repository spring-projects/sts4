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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.parameters.editors;

import java.util.ArrayList;
import java.util.List;

import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameter;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameterDescriptor;


/**
 * @author Nieraj Singh
 * @author Christian Dupuis
 */
public abstract class AbstractParameterEditor implements IParameterEditor,
		IUIChangeListener {

	private ICommandParameter parameter;
	private List<IUIChangeListener> listeners;
	private boolean requiresLabel;

	public AbstractParameterEditor(ICommandParameter parameter, boolean requiresLabel) {
		this.parameter = parameter;
		this.requiresLabel = requiresLabel;
	}

	public boolean requiresParameterNameLabel() {
		return requiresLabel;
	}

	public IUIChangeListener addUIChangeListener(IUIChangeListener listener) {
		if (listener == null) {
			return null;
		}
		if (listeners == null) {
			listeners = new ArrayList<IUIChangeListener>();
		}

		if (listeners.contains(listener)) {
			return null;
		}

		listeners.add(listener);

		return listener;
	}

	public IUIChangeListener removeUIChangeListener(IUIChangeListener listener) {
		if (listeners != null && listeners.remove(listener)) {
			return listener;
		}
		return null;
	}

	public ICommandParameterDescriptor getParameterDescriptor() {
		return parameter.getParameterDescriptor();
	}

	public ICommandParameter getParameter() {
		return parameter;
	}

	protected void setParameterValueAndNotifyClear(Object value) {
		setParameterValue(value, UIEvent.VALUE_SET | UIEvent.CLEAR_VALUE_EVENT);
	}

	protected void setParameterValue(Object value, int eventType) {
		ICommandParameter parameter = getParameter();
		if (parameter != null) {
			parameter.setValue(value);
			notifyListeners(value, eventType);
		}
	}

	protected void notifyListeners(Object eventData, int type) {

		UIEvent event = new UIEvent(eventData, this, type);
		if (listeners != null) {
			for (IUIChangeListener listener : listeners) {
				listener.handleUIEvent(event);
			}
		}
	}


	public void handleUIEvent(UIEvent event) {
		if ((event.getType() & UIEvent.CLEAR_VALUE_EVENT) == UIEvent.CLEAR_VALUE_EVENT) {
			setParameterValue(null, UIEvent.VALUE_SET);
			clearControls();
		}
	}

	/**
	 * Subclasses must implement when a clear control event is received, to
	 * clear all values in any editor control where values are set
	 */
	abstract protected void clearControls();

}
