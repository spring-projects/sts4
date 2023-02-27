/*******************************************************************************
 * Copyright (c) 2016, 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.languageserver;

import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressReport;

public interface ProgressService {

	public static ProgressService NO_PROGRESS = new ProgressService() {
		
		@Override
		public void progressBegin(String taskId, WorkDoneProgressBegin report) {
		}

		@Override
		public void progressEvent(String taskId, WorkDoneProgressReport report) {
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
	 * @deprecated Use {@link #progressBegin(String, WorkDoneProgressBegin)}
	 */
	default void progressBegin(String taskId, String title, String message) {
		WorkDoneProgressBegin report = new WorkDoneProgressBegin();
		report.setCancellable(false);
		if (message != null && !message.isEmpty()) {
			report.setMessage(message);
		}
		report.setTitle(title);
		
		progressBegin(taskId, report);
	}
	
	/**
	 * Sends an event to start progress to the LSP client.
	 * 
	 * @param taskId is an arbitrary id
	 * that can be chosen by the caller. The purpose of the id is to be a 'unique'
	 * id for some kind of 'long running job'
	 * @param report the report either string messages or percentage
	 */
	void progressBegin(String taskId, WorkDoneProgressBegin report);
	
	/**
	 * Sends a progress event to the LSP client. Each event updates the message shown
	 * to the user replacing the old one.
	 * More than one message may be shown simultaneously to the user, if they
	 * have different taskId.
	 *
	 * @param taskId
	 * @param statusMsg
	 * 
	 * @deprecated Use {@link #progressEvent(String, WorkDoneProgressReport)}
	 */
	default void progressEvent(String taskId, String statusMsg) {
		WorkDoneProgressReport report = new WorkDoneProgressReport();
		report.setMessage(statusMsg);
		progressEvent(taskId, report);
	}
	
	/**
	 * Sends a progress event to the LSP client. Each event updates the message shown
	 * to the user replacing the old one.
	 * More than one message may be shown simultaneously to the user, if they
	 * have different taskId.
	 *
	 * @param taskId
	 * @param report
	 */
	void progressEvent(String taskId, WorkDoneProgressReport report);
	
	/**
	 * Send the event to the LSP client to end progress for passed id
	 * 
	 * @param taskId the id of the task in progress
	 */
	void progressDone(String taskId);
		
	default IndefiniteProgressTask createIndefiniteProgressTask(String taskId, String title, String message) {
		return new IndefiniteProgressTask(taskId, this, title, message);
	}
	
	default PercentageProgressTask createPercentageProgressTask(String taskId, int totalWork, String title) {
		return new PercentageProgressTask(taskId, this, totalWork, title);
	}

}
