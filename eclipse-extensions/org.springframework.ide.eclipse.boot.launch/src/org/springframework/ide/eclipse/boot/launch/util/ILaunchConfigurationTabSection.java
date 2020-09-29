/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch.util;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;

/**
 * Additional API that a IPageSection may wish to implement when it
 * is placed inside a {@link LaunchConfigurationTabWithSections}.
 *
 * @author Kris De Volder
 */
public interface ILaunchConfigurationTabSection extends IPageSection {

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
	LiveExpression<Boolean> getDirtyState();

}
