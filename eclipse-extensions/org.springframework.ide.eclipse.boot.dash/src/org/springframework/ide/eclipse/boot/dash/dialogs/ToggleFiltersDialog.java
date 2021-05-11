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
package org.springframework.ide.eclipse.boot.dash.dialogs;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseMultipleSection;
import org.springsource.ide.eclipse.commons.livexp.ui.DescriptionSection;
import org.springsource.ide.eclipse.commons.livexp.ui.DialogWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

public class ToggleFiltersDialog extends DialogWithSections {

	private ToggleFiltersDialogModel model;

	public ToggleFiltersDialog(String title, ToggleFiltersDialogModel model, Shell shell) {
		super(title, model, shell);
		this.model = model;
		// TODO Auto-generated constructor stub
	}

	public static void open(ToggleFiltersDialogModel model, Shell shell) {
		ToggleFiltersDialog dlg = new ToggleFiltersDialog("Customize Filters", model, shell);
		dlg.open();
	}

	@Override
	protected List<WizardPageSection> createSections() throws CoreException {
		return Arrays.asList(
				new StringFieldSection(this, "Regex to match names of the elements to be shown", model.getRegExFilterLiveVar(), Validator.OK).vertical(true),
				new ChooseMultipleSection<>(this,
						"Filters",
						model.getAvailableFilters(),
						model.getSelectedFilters(),
						Validator.OK,
						model.getSelectedLiveVar()).vertical(true),
				new DescriptionSection(this, model.getToggleFilterDescription())
		);
	}

}
