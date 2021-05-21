/*******************************************************************************
 * Copyright (c) 2015, 2021 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.dialogs;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.ui.ButtonSection;
import org.springsource.ide.eclipse.commons.livexp.ui.CheckboxSection;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseMultipleSection;
import org.springsource.ide.eclipse.commons.livexp.ui.DescriptionSection;
import org.springsource.ide.eclipse.commons.livexp.ui.RowSection;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.TrayDialogWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

public class ToggleFiltersDialog extends TrayDialogWithSections {

	private ToggleFiltersDialogModel model;

	public ToggleFiltersDialog(String title, ToggleFiltersDialogModel model, Shell shell) {
		super(shell, model);
		this.model = model;
	}

	@Override
	protected Control createContents(Composite parent) {
		getShell().setText("Select Filters");
		return super.createContents(parent);
	}

	public static void open(ToggleFiltersDialogModel model, Shell shell) {
		ToggleFiltersDialog dlg = new ToggleFiltersDialog("Customize Filters", model, shell);
		dlg.open();
	}

	@Override
	protected List<WizardPageSection> createSections() throws CoreException {
		return Arrays.asList(
				new CheckboxSection(this, model.getEnableRegexFilter(), "Name filter patterns (matching names will be hidden):"),
				new StringFieldSection(this,
						"Regex pattern to match names of the elements to be hidden",
						model.getRegExFilterLiveVar(),
						Validator.OK).labelUnder(true).setEnabler(model.getEnableRegexFilter()),
				new ChooseMultipleSection<>(this,
						"Filters",
						model.getAvailableFilters(),
						model.getSelectedFilters(),
						Validator.OK,
						model.getSelectedLiveVar()).vertical(true),
				new DescriptionSection(this, model.getToggleFilterDescription()),
				new RowSection(this,
						new ButtonSection(this, "Select All", model::selectAll),
						new ButtonSection(this, "Deselect All", model::deselectAll)
				)
		);
	}

}
