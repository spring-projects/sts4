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
package org.springframework.ide.eclipse.boot.dash.cf.debug;

import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.util.NameableLabelProvider;
import org.springframework.ide.eclipse.boot.launch.util.DelegatingLaunchConfigurationTabSection;
import org.springframework.ide.eclipse.boot.launch.util.ILaunchConfigurationTabSection;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;

/**
 * @author Kris De Volder
 */
public class SelectRunTargetLaunchTabSection {

	public static ILaunchConfigurationTabSection create(IPageWithSections owner, SelectRunTargetLaunchTabModel model) {
		ChooseOneSectionCombo<CloudFoundryRunTarget> ui =
				new ChooseOneSectionCombo<CloudFoundryRunTarget>(owner, "Target", model, model.getChoices());
		ui.setLabelProvider(new NameableLabelProvider());
		return new DelegatingLaunchConfigurationTabSection(owner, model, ui);
	}


}
