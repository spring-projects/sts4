/*******************************************************************************
 * Copyright (c) 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch;


import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.debug.ui.PrototypeTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.launching.JavaRuntime;

public class BootLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	/**
	 * @see ILaunchConfigurationTabGroup#createTabs(ILaunchConfigurationDialog, String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfiguration configuration = DebugUITools.getLaunchConfiguration(dialog);
		boolean isModularConfiguration = configuration != null && JavaRuntime.isModularConfiguration(configuration);
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
			new BootMainTab(),
			new JavaArgumentsTab(),
			new JavaJRETab(),
			isModularConfiguration ? new BootDependenciesTab() : new BootClasspathTab(),
			new SourceLookupTab(),
			new EnvironmentTab(),
			new CommonTab(),
			new PrototypeTab()
		};
		setTabs(tabs);
	}

}
