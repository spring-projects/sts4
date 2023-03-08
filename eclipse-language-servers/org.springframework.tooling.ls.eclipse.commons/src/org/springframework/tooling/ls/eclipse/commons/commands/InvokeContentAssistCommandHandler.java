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
package org.springframework.tooling.ls.eclipse.commons.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.progress.UIJob;

public class InvokeContentAssistCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		final IHandlerService service = workbenchWindow.getService(IHandlerService.class);
		Display display = workbenchWindow.getWorkbench().getDisplay();
		Assert.isLegal(display != null && !display.isDisposed());
		UIJob job = new UIJob(display, "Invoke Content Assist") {

			@Override
			public IStatus runInUIThread(IProgressMonitor arg0) {
				try {
					service.executeCommand(IWorkbenchCommandConstants.EDIT_CONTENT_ASSIST, null);
					return Status.OK_STATUS;
				} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
					return Status.error("Failed to invoke content assist", e);
				}
			}

		};
		job.schedule();
		return null;
	}

}
