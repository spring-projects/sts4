/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.actions;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;

/**
 * Action for starting/restarting Remove DevTools Client application
 *
 * @author Alex Boyko
 *
 */
public class CloudFoundryRestartWithRemoteDevClientAction extends AbstractCloudAppDashElementsAction {

	private ElementStateListener stateListener;

	public CloudFoundryRestartWithRemoteDevClientAction(Params params) {
		super(params);
		this.setText("(Re)start Remote DevTools Client");
		this.setToolTipText("Restarts application with the Remote DevTools Client attched.");
		URL url = FileLocator.find(Platform.getBundle("org.springframework.ide.eclipse.boot"), new Path("resources/icons/boot-devtools-icon.png"), null);
		if (url != null) {
			this.setImageDescriptor(ImageDescriptor.createFromURL(url));
		}
		if (model != null) {
			model.addElementStateListener(stateListener = new ElementStateListener() {
				public void stateChanged(BootDashElement e) {
					if (getSelectedElements().contains(e) && !PlatformUI.getWorkbench().isClosing()) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								updateEnablement();
							}
						});
					}
				}
			});
		}
	}

	@Override
	public void updateEnablement() {
		boolean enable = false;
		if (!getSelectedElements().isEmpty()) {
			enable = true;
			for (BootDashElement e : getSelectedElements()) {
				if (!(e instanceof CloudAppDashElement) || e.getProject() == null) {
					enable = false;
				}
			}
		}
		this.setEnabled(enable);
	}

	@Override
	public void run() {
		for (BootDashElement _e : getSelectedElements()) {
			if (_e instanceof CloudAppDashElement && _e.getBootDashModel() instanceof CloudFoundryBootDashModel && _e.getProject() != null) {
				CloudAppDashElement e = (CloudAppDashElement) _e;
				e.restartWithRemoteClient(ui(), e.createCancelationToken());
			}
		}
	}

	@Override
	public void dispose() {
		if (model != null && stateListener != null) {
			model.removeElementStateListener(stateListener);
			stateListener = null;
		}
		super.dispose();
	}

}
