/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
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
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class EnableJmxSshTunnelAction extends AbstractCloudAppDashElementsAction {

	private ElementStateListener stateListener;

	public EnableJmxSshTunnelAction(Params params) {
		super(params);
		this.setText("Enable JMX Ssh Tunneling");
		this.setToolTipText("Enables JMX on this app and creates an SSH Tunnel the next time the app is started.");
		URL url = FileLocator.find(Platform.getBundle("org.springframework.ide.eclipse.boot.dash"), new Path("icons/link_to_editor.png"), null);
		if (url != null) {
			this.setImageDescriptor(ImageDescriptor.createFromURL(url));
		}
		if (model != null) {
			model.addElementStateListener(stateListener = new ElementStateListener() {
				@Override
				public void stateChanged(BootDashElement e) {
					if (getSelectedElements().contains(e) && !PlatformUI.getWorkbench().isClosing()) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							@Override
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
		boolean enableTunnel = false;
		if (getSelectedElements().size()==1) {
			BootDashElement e = getSelectedElements().iterator().next();
			if (e instanceof CloudAppDashElement && e.getProject() != null) {
				CloudAppDashElement cde = (CloudAppDashElement) e;
				enable = true;
				enableTunnel = !cde.getEnableJmxSshTunnel();
			}
		}
		this.setEnabled(enable);
		if (enable) {
			setText((enableTunnel ? "Enable" : "Disable")+" JMX Ssh Tunnelling");
		} else {
			setText("Toggle JMX Ssh Tunnelling");
		}
	}

	@Override
	public void run() {
		try {
			for (BootDashElement _e : getSelectedElements()) {
				if (_e instanceof CloudAppDashElement && _e.getBootDashModel() instanceof CloudFoundryBootDashModel && _e.getProject() != null) {
					CloudAppDashElement e = (CloudAppDashElement) _e;
					boolean enable = !e.getEnableJmxSshTunnel();
					e.setEnableJmxSshTunnel(enable);
					RunState runstate = e.getRunState();
					if (runstate!=RunState.INACTIVE) {
						int answer = ui().confirmOperation((enable ? "Enabling" : "Disabling") +" JMX Requires Restart",
								"Enabling JMX is done by setting environment parameters on startup. For this setting to take effect the app needs to be restarted.\n\n" +
								"Do you want to restart now?",
								new String[] {
										"Yes, Restart NOW",
										"No, I'll restart MANUALLY later"
								},
								0
						);
						if (answer==0) {
							RunState runningOrDebugging = runstate==RunState.DEBUGGING ? RunState.DEBUGGING : RunState.RUNNING;
							e.restart(runningOrDebugging, ui());
						}
					}
				}
			}
		} catch (Exception e) {
			Log.log(e);
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
