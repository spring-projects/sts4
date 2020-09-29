/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui.handlers;

import org.eclipse.debug.core.DebugException;
import org.springsource.ide.eclipse.commons.ui.launch.LaunchList;
import org.springsource.ide.eclipse.commons.ui.launch.LaunchList.Item;
import org.springsource.ide.eclipse.commons.ui.launch.LaunchUtils;
import org.springsource.ide.eclipse.commons.ui.launch.LiveAndDeadProcessTracker;

/**
 * Handler for re-launching application
 *
 * @author Alex Boyko
 *
 */
public class RelaunchHandler extends AbstractLaunchHandler {

	@Override
	protected void performOperation(LaunchList.Item l) throws DebugException {
		LaunchUtils.terminateAndRelaunch(l.conf, l.mode);
	}

	@Override
	protected String getOperationName() {
		return "Relaunch";
	}

	@Override
	protected Item getLaunchItem() {
		return LiveAndDeadProcessTracker.getInstance().getLast();
	}

}
