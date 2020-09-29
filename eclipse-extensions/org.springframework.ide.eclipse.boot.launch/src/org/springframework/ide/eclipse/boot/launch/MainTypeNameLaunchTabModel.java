/*******************************************************************************
 * Copyright (c) 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.launch.BootLaunchUIModel.MainTypeValidator;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

/**
 * @author Kris De Volder
 */
public class MainTypeNameLaunchTabModel extends LaunchTabSelectionModel<String> {

	private MainTypeNameLaunchTabModel(LiveVariable<String> selection,
			LiveExpression<ValidationResult> validator) {
		super(selection, validator);
	}

	public static MainTypeNameLaunchTabModel create() {
		LiveVariable<String> n = new LiveVariable<>("");
		MainTypeValidator nv = new MainTypeValidator(n);
		return new MainTypeNameLaunchTabModel(n, nv);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		try {
			selection.setValue(config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, ""));
			getDirtyState().setValue(false);
		} catch (Exception e) {
			BootActivator.log(e);
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) {
		BootLaunchConfigurationDelegate.setMainType(config, StringUtil.trim(selection.getValue()));
		getDirtyState().setValue(false);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		BootLaunchConfigurationDelegate.setMainType(config, "");
	}

}
