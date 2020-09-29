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
import org.eclipse.swt.widgets.Composite;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

/**
 * Implementation of {@link ILaunchConfigurationTabSection} that wraps and delegates
 * to two objects, one representing the 'model' and the other the 'ui'.
 *
 * @author Kris De Volder
 */
public class DelegatingLaunchConfigurationTabSection extends WizardPageSection implements ILaunchConfigurationTabSection {

	private ILaunchConfigurationTabModel model;
	private IPageSection ui;

	public DelegatingLaunchConfigurationTabSection(IPageWithSections owner, ILaunchConfigurationTabModel model, IPageSection ui) {
		super(owner);
		this.model = model;
		this.ui = ui;
	}

	@Override
	final public LiveVariable<Boolean> getDirtyState() {
		return model.getDirtyState();
	}

	@Override
	final public void initializeFrom(ILaunchConfiguration conf) {
		model.initializeFrom(conf);
	}

	@Override
	final public void performApply(ILaunchConfigurationWorkingCopy conf) {
		model.performApply(conf);
	}

	@Override
	final public void setDefaults(ILaunchConfigurationWorkingCopy conf) {
		model.setDefaults(conf);
	}

	final public void dispose() {
		if (ui instanceof Disposable) {
			((Disposable) ui).dispose();
			ui = null;
		}
	}

	@Override
	public void createContents(Composite page) {
		ui.createContents(page);
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return ui.getValidator();
	}


}
