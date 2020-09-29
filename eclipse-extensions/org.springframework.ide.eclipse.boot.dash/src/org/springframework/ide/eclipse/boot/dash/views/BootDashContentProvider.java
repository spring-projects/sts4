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
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;

/**
 * @author Kris De Volder
 */
public class BootDashContentProvider implements IStructuredContentProvider {

	private BootDashModel model;

	public BootDashContentProvider(BootDashModel model) {
		this.model = model;
	}
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}
	public void dispose() {
		//Actually we don't own the model but 'borrow' it. So don't dispose
	}
	public Object[] getElements(Object parent) {
		Object[] es = model.getElements().getValue().toArray();
		return es;
	}
}