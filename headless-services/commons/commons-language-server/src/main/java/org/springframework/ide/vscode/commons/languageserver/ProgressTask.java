/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver;

/**
 * Writes progress messages to the same progress task.
 * 
 * This handler can be used for long-running progress that requires message updates to the same task.
 *
 */
public class ProgressTask {
	
	private final String taskId;
	private final ProgressService service;
	
	
	public ProgressTask(String taskId, ProgressService service) {
		this.taskId = taskId;
		this.service = service;
	}

	public void progressEvent(String statusMsg) {
		this.service.progressEvent(taskId, statusMsg);
	}
	
	public void progressDone() {
		this.service.progressDone(taskId);
	}

}
