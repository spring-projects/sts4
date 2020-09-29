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
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import static org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode.SYNC;

import org.springframework.ide.eclipse.boot.dash.model.AbstractBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;

/**
 * We use mostly use mockito to mock BootDashModel, but some test cases need
 * a more fleshed out mock (e.g. to test listeners attached to the model).
 *
 * @author Kris De Volder
 */
public class MockBootDashModel extends AbstractBootDashModel {

	public MockBootDashModel(RunTarget target, BootDashModelContext context, BootDashViewModel parent) {
		super(target, parent);
	}

	private LiveSetVariable<BootDashElement> elements = new LiveSetVariable<>(SYNC);

	@Override
	public ObservableSet<BootDashElement> getElements() {
		return elements;
	}

	@Override
	public BootDashModelConsoleManager getElementConsoleManager() {
		return null;
	}

	@Override
	public void dispose() {
		elements = null;
	}

	@Override
	public void refresh(UserInteractions ui) {
	}

	public void add(BootDashElement element) {
		elements.add(element);
	}
}
