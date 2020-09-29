/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.dialogs;

import org.eclipse.jface.wizard.Wizard;

/**
 * Creates a new server instance from a selected space in an organization and
 * spaces viewer in the wizard. It only creates the server instance if another
 * server instance to that space does not exist.
 */
public class OrgsAndSpacesWizard extends Wizard {

	private OrgsAndSpacesWizardPage cloudSpacePage;

	private CloudFoundryTargetWizardModel properties;

	public OrgsAndSpacesWizard(
			CloudFoundryTargetWizardModel targetProperties) {
		cloudSpacePage = new OrgsAndSpacesWizardPage(targetProperties);
		this.properties = targetProperties;
		setWindowTitle("Select space for: " + targetProperties.getUrl());
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		cloudSpacePage.setWizard(this);
		addPage(cloudSpacePage);
	}

	@Override
	public boolean performFinish() {
		return properties.getSpaceName() != null && properties.getOrganizationName() != null;
	}
}
