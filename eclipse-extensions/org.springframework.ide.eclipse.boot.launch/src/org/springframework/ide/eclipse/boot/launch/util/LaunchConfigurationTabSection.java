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

import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

/**
 * Abstract base clase to implement ILaunchConfigurationTabSection.
 *
 * @author Kris De Volder
 */
public abstract class LaunchConfigurationTabSection extends WizardPageSection implements ILaunchConfigurationTabSection {

	private LiveVariable<Boolean> dirtyState = new LiveVariable<Boolean>(true);

	public LaunchConfigurationTabSection(IPageWithSections owner) {
		super(owner);
	}

	@Override
	public LiveVariable<Boolean> getDirtyState() {
		return dirtyState;
	}

}
