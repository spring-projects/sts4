/*******************************************************************************
 * Copyright (c) 2013, 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch.cli;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class BootGroovyScriptLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup {

	public BootGroovyScriptLaunchConfigurationTabGroup() {
	}

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
//				new SpringCommandTab()
//				new AppletParametersTab(),
//				new JavaArgumentsTab(),
//				new JavaJRETab(),
//				new JavaClasspathTab(),
//				new CommonTab()
		};
		setTabs(tabs);

	}

}
