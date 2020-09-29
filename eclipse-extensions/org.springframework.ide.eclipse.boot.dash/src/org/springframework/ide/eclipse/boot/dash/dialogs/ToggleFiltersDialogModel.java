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
package org.springframework.ide.eclipse.boot.dash.dialogs;

import org.springframework.ide.eclipse.boot.dash.model.ToggleFiltersModel;
import org.springframework.ide.eclipse.boot.dash.model.ToggleFiltersModel.FilterChoice;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
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
	private LiveSet<FilterChoice> selectedFilters = new LiveSet<FilterChoice>();

	public ToggleFiltersDialogModel(ToggleFiltersModel viewModel) {
		this.viewModel = viewModel;
		selectedFilters.replaceAll(viewModel.getSelectedFilters().getValue());
	}

	public FilterChoice[] getAvailableFilters() {
		return viewModel.getAvailableFilters();
	}

	public LiveSet<FilterChoice> getSelectedFilters() {
		return selectedFilters;
	}

	@Override
	public void performOk() throws Exception {
		viewModel.getSelectedFilters().replaceAll(selectedFilters.getValue());
	}
}
