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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.Wizard;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;

/**
 *
 */
public class SelectMainTypeWizard extends Wizard {

	private final List<IType> mainTypes;

	private SelectMainTypeWizardPage page;

	public SelectMainTypeWizard(List<IType> mainTypes) {
		this.mainTypes = mainTypes;
	}

	@Override
	public void addPages() {
		page = new SelectMainTypeWizardPage(mainTypes,
				BootDashActivator.getImageDescriptor("icons/wizban_cloudfoundry.png"));
		addPage(page);
	}

	public boolean performFinish() {
		return page != null && page.getSelectedMainType() != null;
	}

	public IType getSelectedMainType() {
		return page != null ? page.getSelectedMainType() : null;
	}
}
