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
package org.springframework.ide.eclipse.boot.dash.cf.dialogs;

import java.util.concurrent.Callable;

import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springsource.ide.eclipse.commons.livexp.core.StringFieldModel;
import org.springsource.ide.eclipse.commons.livexp.ui.OkButtonHandler;

/**
 * @author Martin Lippert
 */
public class CustomizeAppsManagerURLDialogModel implements OkButtonHandler {

	public final StringFieldModel host = new StringFieldModel("Apps Manager", "");
	private final CloudFoundryBootDashModel cloudFoundrySection;

	public CustomizeAppsManagerURLDialogModel(CloudFoundryBootDashModel cloudFoundrySection) {
		this.cloudFoundrySection = cloudFoundrySection;
		host.getVariable().setValue(this.cloudFoundrySection.getRunTarget().getAppsManagerHost());
	}

	public Callable<Void> restoreDefaultsHandler = new Callable<Void>() {
		public Void call() throws Exception {
			host.getVariable().setValue(getDefaultValue());
			return null;
		}
	};

	@Override
	public void performOk() throws Exception {
		cloudFoundrySection.getRunTarget().setAppsManagerHost(host.getValue());
		cloudFoundrySection.notifyModelStateChanged();
	}

	public String getHelpText() {
		return "no help available";
	}

	public String getDefaultValue() {
		return cloudFoundrySection.getRunTarget().getAppsManagerHostDefault();
	}

}
