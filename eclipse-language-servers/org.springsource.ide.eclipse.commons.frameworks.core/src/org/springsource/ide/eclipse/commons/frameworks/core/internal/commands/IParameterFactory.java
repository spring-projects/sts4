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
 * Allows specific domains to implement their own parameter instance factory that gets invoked by
 * the command framework.
 * @author Nieraj Singh
 */
public interface IParameterFactory {
	
	/**
	 * Given a parameter descriptor, generate a parameter instance. This method is typically called by the framework so clients
	 * only need to make sure that their descriptor adapts to their factory for the generic command framework to invoke the factory.
	 * 
	 * @param descriptor
	 * @return command parameter instance corresponding to the descriptor
	 */
	public ICommandParameter getParameter(ICommandParameterDescriptor descriptor);

}
