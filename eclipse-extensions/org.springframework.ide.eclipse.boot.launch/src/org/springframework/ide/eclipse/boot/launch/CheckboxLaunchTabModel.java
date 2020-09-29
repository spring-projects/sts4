/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;

/**
 * Basic model for a checkbox who's value is stored into a launch configuration attribute.
 *
 * @author Kris De Volder
 */
public class CheckboxLaunchTabModel extends LaunchTabSelectionModel<Boolean> {

	public static CheckboxLaunchTabModel create(String attributeName, boolean defaultValue, LiveExpression<ValidationResult> validator) {
		LiveVariable<Boolean> enable = new LiveVariable<>();
		return new CheckboxLaunchTabModel(attributeName, defaultValue, enable, validator);
	}

	public static CheckboxLaunchTabModel create(String attributeName, boolean defaultValue) {
		LiveVariable<Boolean> enable = new LiveVariable<>();
		return new CheckboxLaunchTabModel(attributeName, defaultValue, enable, Validator.OK);
	}

	private String attributeName;
	private boolean defaultValue;

	protected CheckboxLaunchTabModel(String attributeName, boolean defaultValue, LiveVariable<Boolean> selection, LiveExpression<ValidationResult> validator) {
		super(selection, validator);
		this.attributeName = attributeName;
		this.defaultValue = defaultValue;
	}

	@Override
	public void initializeFrom(ILaunchConfiguration conf) {
		selection.setValue(getAttribute(conf));
		getDirtyState().setValue(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy conf) {
		setAttribute(conf, selection.getValue());
		getDirtyState().setValue(false);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy conf) {
		setAttribute(conf, defaultValue);
	}

	protected boolean getAttribute(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(attributeName, defaultValue);
		} catch (CoreException e) {
			BootActivator.log(e);
		}
		return defaultValue;
	}

	protected void setAttribute(ILaunchConfigurationWorkingCopy conf, Boolean value) {
		conf.setAttribute(attributeName, value);
	}
}
