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
package org.springframework.ide.eclipse.boot.launch;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.launch.util.DelegatingLaunchConfigurationTabSection;
import org.springframework.ide.eclipse.boot.launch.util.ILaunchConfigurationTabSection;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.SimpleLabelProvider;

/**
 * @author Kris De Volder
 */
public class SelectProjectLaunchTabSection {

	public static ILaunchConfigurationTabSection create(IPageWithSections owner, SelectProjectLaunchTabModel model) {
		ChooseOneSectionCombo<IProject> ui =
				new ChooseOneSectionCombo<IProject>(owner, "Project", model, model.interestingProjects());
		//allowTextEdits(ProjectNameParser.INSTANCE);
		ui.setLabelProvider(new SimpleLabelProvider() {
			public String getText(Object element) {
				if (element instanceof IProject) {
					return ((IProject) element).getName();
				}
				return super.getText(element);
			}
		});
		return new DelegatingLaunchConfigurationTabSection(owner, model, ui);
	}


}
