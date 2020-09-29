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
package org.springframework.ide.eclipse.boot.dash.views.sections;

import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.ButtonModel;

import com.google.common.collect.ImmutableSet;

/**
 * @author Kris De Volder
 */
public class BootDashTreeContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object e) {
		return getChildren(e);
	}

	@Override
	public Object[] getChildren(Object e) {
		if (e instanceof BootDashViewModel) {
			return ((BootDashViewModel) e).getSectionModels().getValue().toArray();
		} else if (e instanceof BootDashModel) {
			BootDashModel model = ((BootDashModel) e);
			ImmutableSet<BootDashElement> bdes = model.getElements().getValue();
			ImmutableSet<ButtonModel> buttons = model.getButtons().getValue();
			if (buttons.isEmpty()) {
				//common special case, avoid extra intermediate collection creation
				return bdes.toArray();
			}
			ArrayList<Object> merged = new ArrayList<>(bdes.size()+buttons.size());
			merged.addAll(buttons);
			merged.addAll(bdes);
			return merged.toArray();
		} else if (e instanceof BootDashElement) {
			return ((BootDashElement)e).getChildren().getValues().toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object e) {
		if (e instanceof BootDashElement) {
			return ((BootDashElement) e).getParent();
		} else if (e instanceof BootDashModel) {
			return ((BootDashModel) e).getViewModel();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object e) {
		return ArrayUtils.isNotEmpty(getChildren(e));
	}

}
