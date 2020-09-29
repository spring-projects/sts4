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
 * Notifies listeners when parameter values have changed.
 * @author Nieraj Singh
 */
public interface IValueChangedNotifier {

	public IParameterValueListener addListener(IParameterValueListener listener);

	public IParameterValueListener removeListener(
			IParameterValueListener listener);

}
