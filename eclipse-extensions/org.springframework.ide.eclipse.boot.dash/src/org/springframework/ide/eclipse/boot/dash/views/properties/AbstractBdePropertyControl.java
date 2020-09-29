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
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.labels.BootDashLabels;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;

/**
 * Abstract common implementation for the Boot Dash Element feature control for
 * the properties view section
 *
 * @author Alex Boyko
 * @author Kris De Volder
 */
public abstract class AbstractBdePropertyControl implements BootDashElementPropertyControl {

	private BootDashElement bde = null;
	private Stylers stylers;
	private BootDashLabels bdeLabels;

	public void setInput(BootDashElement bde) {
		if (bde != this.bde) {
			this.bde = bde;
		}
	}

	final protected BootDashElement getBootDashElement() {
		return bde;
	}

	final protected BootDashLabels getLabels() {
		return bdeLabels;
	}

	@Override
	public void dispose() {
		bdeLabels.dispose();
		stylers.dispose();
	}

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		stylers = new Stylers(composite.getFont());
		bdeLabels = new BootDashLabels(BootDashActivator.getDefault().getInjections(), stylers);
		bdeLabels.setDeclutter(false);
	}

	protected Stylers getStylers() {
		return stylers;
	}

}
