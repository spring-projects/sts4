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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.springsource.ide.eclipse.commons.livexp.ui.ButtonSection;
import org.springsource.ide.eclipse.commons.livexp.ui.DialogWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * @author Martin Lippert
 */
public class CustomizeAppsManagerURLDialog extends DialogWithSections {

	private CustomizeAppsManagerURLDialogModel model;

	public CustomizeAppsManagerURLDialog(CustomizeAppsManagerURLDialogModel model, Shell shell) {
		super("Customize Host URL of Apps Manager", model, shell);
		this.model = model;
	}

	@Override
	protected List<WizardPageSection> createSections() throws CoreException {
		Builder<WizardPageSection> sections = ImmutableList.builder();
		sections.add(new StringFieldSection(this, model.host).tooltip(model.getHelpText()));
		sections.add(new ButtonSection(this, "Restore Defaults", model.restoreDefaultsHandler));
		return sections.build();
	}

}
