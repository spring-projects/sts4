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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.tasks;



/**
 * @author Nieraj Singh
 */
public class TaskManager {

	private TaskManager() {
		// Util class
	}

	public static void runSynch(IUIRunnable runnable, String taskName) {
		if (runnable == null) {
			return;
		}
		new SynchUITask(runnable, taskName).execute();
	}

}
