/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.springsource.ide.eclipse.commons.core.Policy;
import org.springsource.ide.eclipse.commons.internal.ui.UiPlugin;


/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class UiStatusHandler {

	public static void logAndDisplay(IStatus status) {
		logAndDisplay(UiUtil.getShell(), status);
	}

	public static void logAndDisplay(Shell shell, IStatus status) {
		logAndDisplay(shell, "Error", status);
	}

	public static void logAndDisplay(Shell shell, String title, IStatus status) {
		UiPlugin.getDefault().getLog().log(status);

		if (Policy.TEST_MODE) {
			throw new RuntimeException(status.getException());
		}

		if (status.getSeverity() == IStatus.INFO) {
			MessageDialog.openInformation(shell, title, status.getMessage());
		}
		else {
			MessageDialog.openError(shell, title, status.getMessage());
		}
	}

}
