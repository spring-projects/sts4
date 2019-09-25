/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Martin Lippert
 */
public class ChangeDetectionHistory {

	private Map<String, ChangeHistory> changeHistory;

	public ChangeDetectionHistory() {
		this.changeHistory = new HashMap<>();
	}

//	public Change[] checkForChanges(SpringBootApp[] runningApps) {
//		List<Change> result = null;
//
//		for (SpringBootApp runningApp : runningApps) {
//			String virtualID = getVirtualAppID(runningApp);
//
//			if (changeHistory.containsKey(virtualID)) {
//				// the standard case
//				ChangeHistory appHistory = changeHistory.get(virtualID);
//				appHistory.updateProcess(runningApp);
//
//				Change change = appHistory.checkForUpdates();
//				if (change != null) {
//					if (result == null) {
//						result = new ArrayList<>();
//					}
//					result.add(change);
//				}
//			}
//			else {
//				String oldAppID = getOldApp(runningApp, runningApps);
//				if (oldAppID != null) {
//					ChangeHistory oldHistory = changeHistory.remove(oldAppID);
//					oldHistory.updateProcess(runningApp);
//					changeHistory.put(virtualID, oldHistory);
//
//					Change change = oldHistory.checkForUpdates();
//					if (change != null) {
//						if (result == null) {
//							result = new ArrayList<>();
//						}
//						result.add(change);
//					}
//				}
//				else {
//					ChangeHistory newHistory = new ChangeHistory();
//					newHistory.updateProcess(runningApp);
//					changeHistory.put(virtualID, newHistory);
//
//					newHistory.checkForUpdates();
//				}
//			}
//		}
//
//		if (result != null) {
//			return (Change[]) result.toArray(new Change[result.size()]);
//		}
//		else {
//			return null;
//		}
//	}
//
//	private String getVirtualAppID(SpringBootApp app) {
//		return app.getProcessID();
//	}
//
//	private String getOldApp(SpringBootApp app, SpringBootApp[] allApps) {
//		Set<String> histories = this.changeHistory.keySet();
//
//		try {
//			String commandLine = app.getJavaCommand();
//			String[] classpath = app.getClasspath();
//
//			for (String oldProcessID : histories) {
//				if (this.changeHistory.get(oldProcessID).matchesProcess(commandLine, classpath)
//						&& processNotRunningAnymore(oldProcessID, allApps)) {
//					return oldProcessID;
//				}
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return null;
//	}
//
//	private boolean processNotRunningAnymore(String oldProcessID, SpringBootApp[] allApps) {
//		try {
//			for (SpringBootApp app : allApps) {
//				String id = getVirtualAppID(app);
//				if (id != null && id.equals(oldProcessID)) {
//					return false;
//				}
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return true;
//	}

}
