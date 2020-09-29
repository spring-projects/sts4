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

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

public class SelectProjectLaunchTabModel extends LaunchTabSelectionModel<IProject> {

	public static SelectProjectLaunchTabModel create() {
		LiveVariable<IProject> project = new LiveVariable<IProject>();
		ExistingBootProjectSelectionValidator validator = new ExistingBootProjectSelectionValidator(project);
		return new SelectProjectLaunchTabModel(project, validator);
	}

	public SelectProjectLaunchTabModel(LiveVariable<IProject> p,
			LiveExpression<ValidationResult> pv) {
		super(p, pv);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration conf) {
		selection.setValue(BootLaunchConfigurationDelegate.getProject(conf));
		getDirtyState().setValue(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy conf) {
		BootLaunchConfigurationDelegate.setProject(conf, selection.getValue());
		getDirtyState().setValue(false);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy conf) {
		BootLaunchConfigurationDelegate.setProject(conf, null);
	}

	public IProject[] interestingProjects() {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		ArrayList<IProject> interesting = new ArrayList<IProject>(allProjects.length);
		for (IProject p : allProjects) {
			if (isInteresting(p)) {
				interesting.add(p);
			}
		}
		return interesting.toArray(new IProject[interesting.size()]);
	}

	/**
	 * Decides whether given IProject from the workspace is of interest.
	 * Only projects 'of interest' will be available from the project
	 * selector's pull-down menu.
	 */
	protected boolean isInteresting(IProject project) {
		return BootPropertyTester.isBootProject(project);
	}


}