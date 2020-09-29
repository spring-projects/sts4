/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.jface.action.IAction;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.ToggleFiltersModel;
import org.springframework.ide.eclipse.boot.dash.model.ToggleFiltersModel.FilterChoice;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import com.google.common.collect.ImmutableSet;

/**
 * A checkbox-style menu action that enables / disables
 * a particular 'toggle filter'.
 *
 * @author Kris De Volder
 */
public class ToggleFilterAction extends AbstractBootDashAction {

	private ToggleFiltersModel toggleFilters;
	private ValueListener<ImmutableSet<FilterChoice>> selectedFilterListener;
	final private FilterChoice filter;

	public ToggleFilterAction(BootDashViewModel model, FilterChoice filter, SimpleDIContext context) {
		super(context, IAction.AS_CHECK_BOX);
		this.filter = filter;
		this.toggleFilters = model.getToggleFilters();
		this.setText(filter.getLabel());
		model.getToggleFilters().getSelectedFilters().addListener(selectedFilterListener=new ValueListener<ImmutableSet<FilterChoice>>() {
			public void gotValue(LiveExpression<ImmutableSet<FilterChoice>> exp, ImmutableSet<FilterChoice> value) {
				updateCheckedState();
			}
		});
	}

	protected void updateCheckedState() {
		setChecked(getModelCheckedState());
	}

	protected boolean getModelCheckedState() {
		return toggleFilters.getSelectedFilters().getValues().contains(filter);
	}

	protected void setModelCheckedState(boolean checked) {
		LiveSetVariable<FilterChoice> activeFilters = toggleFilters.getSelectedFilters();
		if (checked) {
			activeFilters.add(filter);
		} else {
			activeFilters.remove(filter);
		}
	}

	@Override
	public void run() {
		setModelCheckedState(!getModelCheckedState());
	}

	@Override
	public void dispose() {
		if (selectedFilterListener!=null) {
			toggleFilters.getSelectedFilters().removeListener(selectedFilterListener);
		}
		super.dispose();
	}
}
