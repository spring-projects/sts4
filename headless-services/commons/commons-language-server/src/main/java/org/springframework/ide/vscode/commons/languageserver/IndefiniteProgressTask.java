/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver;

import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressReport;

public class IndefiniteProgressTask extends AbstractProgressTask {
	
	public IndefiniteProgressTask(String taskId, ProgressService service, String title, String message) {
		super(taskId, service);
		progressBegin(title, message);
	}

	private void progressBegin(String title, String message) {
		WorkDoneProgressBegin report = new WorkDoneProgressBegin();
		report.setTitle(title);
		report.setCancellable(false);
		if (message != null && !message.isEmpty()) {
			report.setMessage(message);
		}		
		service.progressBegin(taskId, report);
	}

	public void progressEvent(String statusMsg) {
		WorkDoneProgressReport report = new WorkDoneProgressReport();
		report.setMessage(statusMsg);
		service.progressEvent(taskId, report);
	}
	


}
