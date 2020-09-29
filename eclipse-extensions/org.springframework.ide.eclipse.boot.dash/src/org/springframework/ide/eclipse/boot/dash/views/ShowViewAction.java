/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.util.Log;

/**
 * Action for showing/opening a specific Eclipse view
 *
 * @author Alex Boyko
 *
 */
public class ShowViewAction extends Action {

	private String viewId;

	public ShowViewAction(String viewId) {
		this(viewId, null, null);
	}

	public ShowViewAction(String viewId, String label, ImageDescriptor iconDescriptor) {
		super();
		this.viewId = viewId;
		IViewDescriptor viewDescriptor = PlatformUI.getWorkbench().getViewRegistry().find(viewId);
		if (label == null && viewDescriptor != null) {
			setText("Show " + viewDescriptor.getLabel());
		} else {
			setText(label);
		}
		if (iconDescriptor == null && viewDescriptor != null) {
			setImageDescriptor(viewDescriptor.getImageDescriptor());
		} else {
			setImageDescriptor(iconDescriptor);
		}
	}

	@Override
	public void run() {
		if (viewId == null) {
			BootDashActivator.getDefault().getLog().log(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID, "View ID is missing"));
		} else {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null && window.getActivePage() != null) {
				try {
					window.getActivePage().showView(viewId, null, IWorkbenchPage.VIEW_ACTIVATE);
				} catch (PartInitException e) {
					Log.log(e);
				}
			} else {
				BootDashActivator.getDefault().getLog().log(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID, "Cannot find workbench window with active page"));
			}
		}
	}


}
