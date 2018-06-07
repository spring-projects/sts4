/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;

/**
 * @author Martin Lippert
 */
public class ChangeDetectionHistory {
	
	private Map<String, ChangeHistory> changeHistory;
	
	public ChangeDetectionHistory() {
		this.changeHistory = new HashMap<>();
	}

	public Change checkForChanges(SpringBootApp app) {
		String virtualID = getVirtualAppID(app);
		
		if (!changeHistory.containsKey(virtualID)) {
			ChangeHistory appHistory = new ChangeHistory(virtualID);
			changeHistory.put(virtualID, appHistory);
		}
		
		ChangeHistory appHistory = changeHistory.get(virtualID);
		appHistory.updateProcess(app);
		
		Change result = appHistory.checkForUpdates();
		return result;
	}

	private String getVirtualAppID(SpringBootApp app) {
		// TODO: this needs a lot more work to make this a real VIRTUAL ID which detects the same app
		// running across different app restarts, so the process ID is not the best way to do this (just
		// a temporary solution)
		return app.getProcessID();
	}
	
}
