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
 * Parameter instances can implement to be notified when a parameter value has
 * changed. This is usually used with Composite parameters, where the value
 * change of one child parameter should notify other child parameters
 * @author Nieraj Singh
 */
public interface IParameterValueListener {

	/**
	 * A parameter value has changed, allowing listeners to handle the change
	 * event.
	 * 
	 * @param event
	 */
	public void handleValueChanged(ParameterValueEvent event);

}
