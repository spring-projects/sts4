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

import java.util.ArrayList;
import java.util.List;

import org.springframework.ide.vscode.boot.java.livehover.v2.LiveBean;

/**
 * @author Martin Lippert
 */
public class Change {
	
//	private final SpringBootApp runningApp;

	private List<LiveBean> newBeans;
	private List<LiveBean> deletedBeans;
	
//	public Change(SpringBootApp runningApp) {
//		this.runningApp = runningApp;
//	}
//
//	public SpringBootApp getRunningApp() {
//		return runningApp;
//	}
//	
	public List<LiveBean> getNewBeans() {
		return newBeans;
	}
	
	public List<LiveBean> getDeletedBeans() {
		return deletedBeans;
	}

	public void addDeletedBean(LiveBean bean) {
		if (deletedBeans == null) {
			deletedBeans = new ArrayList<>();
		}
		
		deletedBeans.add(bean);
	}

	public void addNewBean(LiveBean bean) {
		if (newBeans == null) {
			newBeans = new ArrayList<>();
		}
		
		newBeans.add(bean);
	}

}
