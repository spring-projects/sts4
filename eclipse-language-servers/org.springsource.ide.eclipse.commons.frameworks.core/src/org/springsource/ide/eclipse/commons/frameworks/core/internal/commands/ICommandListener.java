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
 * Classes implementing this interface get notified when a command is added.
 * This is meant to allow users to execute the notifier in a worker thread if
 * necessary. A possible usage is to share the ICommandNotifier in a worker
 * thread where commands can be added.
 * <p>
 * Note that this adds command descriptors as opposed to command instances.
 * </p>
 * @author Nieraj Singh
 */
public interface ICommandListener {

	/**
	 * Add a command to the notifier. If null nothing gets added.
	 * 
	 * @param command
	 *           to add. If null nothing gets added.
	 */
	public void addCommandDescriptor(IFrameworkCommandDescriptor commandDescriptor);
}
