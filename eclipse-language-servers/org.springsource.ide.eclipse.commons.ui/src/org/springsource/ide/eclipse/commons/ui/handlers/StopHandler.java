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
import org.springsource.ide.eclipse.commons.ui.launch.LiveProcessTracker;

/**
 * Handler for stopping/terminating application
 *
 * @author Alex Boyko
 *
 */
public class StopHandler extends AbstractLaunchHandler {

	@Override
	protected void performOperation(LaunchList.Item launch) throws DebugException {
		LaunchUtils.terminate(launch.conf);
	}

	@Override
	protected String getOperationName() {
		return "Stop";
	}

	@Override
	protected Item getLaunchItem() {
		return LiveProcessTracker.getInstance().getLast();
	}

}
