/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.Duplicatable;

/**
 * @author Kris De Volder
 */
public class DuplicateConfigAction extends AbstractBootDashElementsAction {

	public DuplicateConfigAction(Params params) {
		super(params);
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/copy.png"));
		this.setText("Duplicate Config");
		this.setToolTipText("Make a copy of this element's LaunchConfiguration");
	}

	@Override
	public void updateEnablement() {
		BootDashElement element = getSingleSelectedElement();
		setEnabled(element != null && element instanceof Duplicatable && ((Duplicatable<?>) element).canDuplicate());
	}

	@Override
	public void updateVisibility() {
		setVisible(getSingleSelectedElement() instanceof Duplicatable);
	}

	@Override
	public void run() {
		BootDashElement _e = getSingleSelectedElement();
		if (_e instanceof Duplicatable<?>) {
			Duplicatable<?> e = (Duplicatable<?>) _e;
			if (e.canDuplicate()) {
				e.duplicate(ui());
			}
		}
	}

}
