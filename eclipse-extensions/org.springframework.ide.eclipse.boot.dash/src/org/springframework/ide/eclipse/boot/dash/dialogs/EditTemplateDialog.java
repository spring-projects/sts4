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
package org.springframework.ide.eclipse.boot.dash.dialogs;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.springsource.ide.eclipse.commons.livexp.ui.ButtonSection;
import org.springsource.ide.eclipse.commons.livexp.ui.CheckboxSection;
import org.springsource.ide.eclipse.commons.livexp.ui.DialogWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class EditTemplateDialog extends DialogWithSections {

	private EditTemplateDialogModel model;

	public EditTemplateDialog(EditTemplateDialogModel model, Shell shell) {
		super(model.getTitle(), model, shell);
		this.model = model;
	}

	@Override
	protected List<WizardPageSection> createSections() throws CoreException {
		Builder<WizardPageSection> sections = ImmutableList.builder();
		sections.add(new StringFieldSection(this, model.template).tooltip(model.getHelpText()));
		sections.add(new CheckboxSection(this, model.applyToAll));
		sections.add(new ButtonSection(this, "Restore Defaults", model.restoreDefaultsHandler));
		return sections.build();
	}

}
