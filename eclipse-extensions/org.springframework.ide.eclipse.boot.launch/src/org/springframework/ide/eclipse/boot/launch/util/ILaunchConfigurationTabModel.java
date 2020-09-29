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
package org.springframework.ide.eclipse.boot.launch.util;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

/**
 * Methods that a 'model' should implement so it can be easily used
 * as a 'supporting model' for wiring up inside of a
 * {@link ILaunchConfigurationTabSection}
 */
public interface ILaunchConfigurationTabModel {

	void initializeFrom(ILaunchConfiguration conf);
	void performApply(ILaunchConfigurationWorkingCopy conf);
	void setDefaults(ILaunchConfigurationWorkingCopy conf);

	/**
	 * Launch config tabs must track their 'dirty state'. This state reflects
	 * whether any of the UI elements contain 'unsaved edits' since the last time
	 * the ui was synched with the 'stored state' via 'initializeFrom' or
	 * 'performApply'.
	 * <p>
	 * This state is used by launch config editor to properly enable/disable
	 * the 'revert' and 'apply' buttons.
	 */
	LiveVariable<Boolean> getDirtyState();

	LiveExpression<ValidationResult> getValidator();

}
