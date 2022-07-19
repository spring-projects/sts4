/*******************************************************************************
 * Copyright (c) 2016, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.languageserver;

public interface ProgressService {

	public static ProgressService NO_PROGRESS = new ProgressService() {
		
		@Override
		public void progressBegin(String taskId, String title, String message) {
		}

		@Override
		public void progressEvent(String taskId, String statusMsg) {
		}

		@Override
		public void progressDone(String taskId) {
		}
		
	};
	
	/**
	 * Sends an event to start progress to the LSP client.
	 * 
	 * @param taskId is an arbitrary id
	 * that can be chosen by the caller. The purpose of the id is to be a 'unique'
	 * id for some kind of 'long running job'
	 * @param title progress main title, i.e. "Indexing", "Loading"
	 * @param message detail for the title, i.e. subtask in progress at the moment
	 */
	void progressBegin(String taskId, String title, String message);
	
	/**
	 * Sends a progress event to the LSP client. Each event updates the message shown
	 * to the user replacing the old one.
	 * More than one message may be shown simultaneously to the user, if they
	 * have different taskId.
	 *
	 * @param taskId
	 * @param statusMsg
	 */
	void progressEvent(String taskId, String statusMsg);
	
	/**
	 * Send the event to the LSP client to end progress for passed id
	 * 
	 * @param taskId the id of the task in progress
	 */
	void progressDone(String taskId);
		
	default ProgressTask createProgressTask(String taskId) {
		return new ProgressTask(taskId, this);
	}

}
