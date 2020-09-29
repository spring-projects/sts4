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
package org.springframework.ide.eclipse.boot.launch.profiles;

import org.springframework.ide.eclipse.boot.launch.ProfileLaunchTabModel;
import org.springframework.ide.eclipse.boot.launch.util.DelegatingLaunchConfigurationTabSection;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.util.Parser;

/**
 * @author Kris De Volder
 */
public class ProfileLaunchTabSection extends DelegatingLaunchConfigurationTabSection {

	public ProfileLaunchTabSection(IPageWithSections owner, ProfileLaunchTabModel model) {
		super(owner, model, createUI(owner, model));
	}

	private static IPageSection createUI(IPageWithSections owner,
			ProfileLaunchTabModel model) {
		ChooseOneSectionCombo<String> profileChooser = new ChooseOneSectionCombo<String>(owner, "Profile",
				model, model.profileOptions());
		profileChooser.allowTextEdits(Parser.IDENTITY);
		return profileChooser;
	}


}
