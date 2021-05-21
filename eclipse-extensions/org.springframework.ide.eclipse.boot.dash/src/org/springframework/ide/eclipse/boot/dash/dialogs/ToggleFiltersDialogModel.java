/*******************************************************************************
 * Copyright (c) 2015, 2021 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.dialogs;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.ide.eclipse.boot.dash.model.ToggleFiltersModel;
import org.springframework.ide.eclipse.boot.dash.model.ToggleFiltersModel.FilterChoice;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.ui.OkButtonHandler;

/**
 * Model for the ToggleFiltersDialog which allows the user to make a selection
 * from a fixed-set of filters that can be toggled on/off by the user.
 *
 * @author Kris De Volder
 */
public class ToggleFiltersDialogModel implements OkButtonHandler {

	/**
	 * Filters in the view (i.e. the ones that are 'active').
	 */
	private ToggleFiltersModel viewModel;

	/**
	 * Filters in the dialog (these get copied to the view when user pressed 'ok').
	 */
	private LiveSet<FilterChoice> selectedFilters = new LiveSet<>();

	private LiveVariable<Boolean> enableRegexFilter = new LiveVariable<>();

	private LiveVariable<String> regexFilter = new LiveVariable<>();

	private LiveVariable<FilterChoice> selected = new LiveVariable<>();

	private LiveExpression<String> toggleFilterDescription = selected.apply(selected -> {
		if (selected == null) {
			return "Select a filter to see the description";
		} else {
			return selected.getDescription();
		}
	});

	public ToggleFiltersDialogModel(ToggleFiltersModel viewModel) {
		this.viewModel = viewModel;
		selectedFilters.replaceAll(viewModel.getSelectedFilters().getValue());
		regexFilter = new LiveVariable<>(viewModel.getRegexFilter().getValue());
		enableRegexFilter = new LiveVariable<>(viewModel.getEnableRegexFilter().getValue());
	}

	public FilterChoice[] getAvailableFilters() {
		return viewModel.getAvailableFilters();
	}

	public LiveSet<FilterChoice> getSelectedFilters() {
		return selectedFilters;
	}

	public LiveVariable<String> getRegExFilterLiveVar() {
		return regexFilter;
	}

	public String getRegexFilter() {
		return regexFilter.getValue();
	}

	public LiveExpression<String> getToggleFilterDescription() {
		return toggleFilterDescription;
	}

	public LiveVariable<FilterChoice> getSelectedLiveVar() {
		return selected;
	}

	public LiveVariable<Boolean> getEnableRegexFilter() {
		return enableRegexFilter;
	}

	@Override
	public void performOk() throws Exception {
		viewModel.getSelectedFilters().replaceAll(selectedFilters.getValue());
		viewModel.getRegexFilter().setValue(getRegexFilter());
		viewModel.getEnableRegexFilter().setValue(enableRegexFilter.getValue());
	}

	public void selectAll() {
		selectedFilters.replaceAll(Arrays.asList(getAvailableFilters()));
	}

	public void deselectAll() {
		selectedFilters.replaceAll(Collections.emptyList());
	}
}
