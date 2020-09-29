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

/**
 * @author Nieraj Singh
 */
public class UIEvent {

	/**
	 * Clears the value in a parameter as well as its corresponding UI control
	 */
	public static int CLEAR_VALUE_EVENT = 0x4;
	
	/**
	 * Indicates that a parameter value has been set in the parameter model.
	 */
	public static int VALUE_SET = 0x8;
	public static int DEFAULT = 0x2;

	private Object eventObject;
	private int type = -1;
	private IUIChangeListener notifier;

	public UIEvent(Object eventObject, IUIChangeListener notifier, int type) {
		this(eventObject, notifier);
		this.type = type;

	}

	public UIEvent(Object eventObject, IUIChangeListener notifier) {
		this.eventObject = eventObject;
		this.notifier = notifier;
	}

	public IUIChangeListener getNotifier() {
		return notifier;
	}

	public int getType() {
		return type;
	}

	public Object getEventData() {
		return eventObject;
	}
}
