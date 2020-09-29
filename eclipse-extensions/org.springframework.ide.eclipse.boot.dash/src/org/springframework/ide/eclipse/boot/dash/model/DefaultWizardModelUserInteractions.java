/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.springframework.ide.eclipse.boot.dash.util.UiUtil;

public class DefaultWizardModelUserInteractions implements WizardModelUserInteractions{
	public void informationPopup(final String title, final String message) {
		// Open dialogue with the error but do not prevent the creation
		// of the target
		UIJob job = new UIJob(title) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				Shell shell = UiUtil.getShell();
				if (shell != null) {
					MessageDialog.openInformation(shell, title, message);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
}
