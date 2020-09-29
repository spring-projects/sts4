/*******************************************************************************
 * Copyright (c) 2015, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

/**
 * Abstract implementation of common functionality for {@link BootDashElement}
 * properties view section
 *
 * @author Alex Boyko
 *
 */
public abstract class AbstractBdePropertiesSection extends AbstractPropertySection implements ElementStateListener {

	private LiveVariable<BootDashElement> bde = new LiveVariable<>();

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		BootDashActivator.getDefault().getModel().addElementStateListener(this);
		super.createControls(parent, aTabbedPropertySheetPage);
	}

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);

		Assert.isTrue(selection instanceof IStructuredSelection);
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		if (structuredSelection.size() > 1) {
			bde.setValue(null);
		} else {
			Object inputObj = structuredSelection.getFirstElement();
			Assert.isTrue(inputObj instanceof BootDashElement);
			bde.setValue((BootDashElement) inputObj);
		}
	}

	@Override
	public void dispose() {
		BootDashActivator.getDefault().getModel().removeElementStateListener(this);
		super.dispose();
	}

	@Override
	public void stateChanged(BootDashElement e) {
		Display display = getPart() == null ? null : getPart().getSite().getShell().getDisplay();
		if (display == null) {
			display = Display.getDefault();
		}
		if (display != null) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					refresh();
				}
			});
		} else {
			refresh();
		}
	}

	final protected BootDashElement getBootDashElement() {
		return bde.getValue();
	}

	final protected LiveExpression<BootDashElement> getBootDashElementLiveExpression() {
		return bde;
	}

}
