/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui.launch;

import org.eclipse.debug.core.DebugException;

public class RelaunchProcessPullDownToolbarDelegate extends AbstractLaunchToolbarPulldown {

	@Override
	protected void performOperation(LaunchList.Item l) throws DebugException {
		LaunchUtils.terminateAndRelaunch(l.conf, l.mode);
	}

	@Override
	protected String getOperationName() {
		return "Relaunch";
	}

	@Override
	protected LaunchList createList() {
		return LiveAndDeadProcessTracker.getInstance();
	}

}
