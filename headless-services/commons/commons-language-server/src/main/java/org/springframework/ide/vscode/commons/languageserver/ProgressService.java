/*******************************************************************************
 * Copyright (c) 2016-2019 Pivotal, Inc.
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
		public void progressEvent(String taskId, String statusMsg) {
			
		}
		
		@Override
		public void progressDone(String taskId) {
		
		}
	};
	
	/**
	 * Sends a progress event to the LSP client. A taskId is an arbirary id
	 * that can be chosen by the caller. The purpose of the id is to be a 'unique'
	 * id for some kind of 'long running job'. Only a single 'statusMsg' is associated
	 * with a given taskId at any one time. Each event updates the message shown
	 * to the user replacing the old one.
	 * <p>
	 * Updating the message to 'null' erases the previous message without showing
	 * a new one.
	 * <p>
	 * More than one message may be shown simultaneously to the user, if they
	 * have different taskId.
	 *
	 * @param taskId
	 * @param statusMsg
	 */
	void progressEvent(String taskId, String statusMsg);
	
	void progressDone(String taskId);
	
	default ProgressTask createProgressTask(String taskId) {
		return new ProgressTask(taskId, this);
	}

}
