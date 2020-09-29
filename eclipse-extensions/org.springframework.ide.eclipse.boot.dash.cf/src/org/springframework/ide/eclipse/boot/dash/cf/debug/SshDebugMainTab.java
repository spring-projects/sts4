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

import static org.springframework.ide.eclipse.boot.ui.BootUIImages.BOOT_DEVTOOLS_ICON;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.launch.SelectProjectLaunchTabSection;
import org.springframework.ide.eclipse.boot.launch.devtools.StringFieldLaunchTabSection;
import org.springframework.ide.eclipse.boot.launch.util.LaunchConfigurationTabWithSections;
import org.springframework.ide.eclipse.boot.ui.BootUIImages;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;

/**
 * @author Kris De Volder
 */
public class SshDebugMainTab extends LaunchConfigurationTabWithSections implements IPageWithSections {

	@Override
	public String getName() {
		return "SSH Tunnel";
	}

	@Override
	public Image getImage() {
		return BootUIImages.getImage(BOOT_DEVTOOLS_ICON);
	}

	@Override
	protected List<IPageSection> createSections() {
		SshDebugLaunchUIModel model = new SshDebugLaunchUIModel(BootDashActivator.getDefault().getModel());
		return Arrays.asList(new IPageSection[] {
				SelectProjectLaunchTabSection.create(this, model.project),
				SelectRunTargetLaunchTabSection.create(this, model.cfTarget),
				StringFieldLaunchTabSection.create(this, model.appName)
		});
	}

}
