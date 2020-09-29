/*******************************************************************************
 * Copyright (c) 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.Scroller;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;

import com.google.common.collect.ImmutableSet;

public class SelectedDependenciesSection extends WizardPageSection {

	private Composite dependencyArea;
	private Scroller scroller;

	private LiveExpression<Filter<Dependency>> filter;

	private Point sizeHint = new Point(SWT.DEFAULT, SWT.DEFAULT);
	private HierarchicalMultiSelectionFieldModel<Dependency> dependencies;

	public SelectedDependenciesSection(IPageWithSections owner, HierarchicalMultiSelectionFieldModel<Dependency> deps) {
		super(owner);
		this.dependencies = deps;
		this.filter = createFilter(deps);
	}

	public SelectedDependenciesSection(IPageWithSections owner, NewSpringBootWizardModel model) {
		this(owner, model.dependencies);
	}

	@Override
	public void createContents(Composite page) {
		scroller = new Scroller(page);
		GridDataFactory.fillDefaults().grab(true, true).hint(sizeHint).applyTo(scroller);
		dependencyArea = scroller.getBody();
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(dependencyArea);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(dependencyArea);

		for (String cat : dependencies.getCategories()) {
			MultiSelectionFieldModel<Dependency> dependencyGroup = dependencies.getContents(cat);
			SelectedSection<Dependency> checkboxesSection = new SelectedSection<>(owner,
					dependencyGroup.getCheckBoxModels(), /* no label */ null);
			checkboxesSection.createContents(dependencyArea);

			this.filter.addListener((exp, value) -> onFilter(checkboxesSection, cat));
		}
	}

	private void onFilter(SelectedSection<Dependency> checkboxesSection, String cat) {

		checkboxesSection.applyFilter(filter.getValue());

		if (checkboxesSection.isCreated()) {
			boolean hasVisible = checkboxesSection.hasVisible();
			checkboxesSection.setVisible(hasVisible);
		}

		layout();
	}

	private void layout() {
		if (dependencyArea != null && !dependencyArea.isDisposed()) {
			dependencyArea.layout(true);
			dependencyArea.getParent().layout(true);
		}
	}

	private static LiveExpression<Filter<Dependency>> createFilter(HierarchicalMultiSelectionFieldModel<Dependency> dependencies) {
		LiveExpression<Filter<Dependency>> filter = new LiveExpression<Filter<Dependency>>() {

			@Override
			protected Filter<Dependency> compute() {
				ImmutableSet<Dependency> currentSelection = ImmutableSet
						.copyOf(dependencies.getCurrentSelection());
				return (dependency) -> currentSelection.contains(dependency);
			}
		};

		ValueListener<Boolean> selectionListener = (exp, val) -> {
			filter.refresh();
		};

		for (String cat : dependencies.getCategories()) {
			MultiSelectionFieldModel<Dependency> dependencyGroup = dependencies.getContents(cat);
			dependencyGroup.addSelectionListener(selectionListener);
		}

		return filter;
	}

	public SelectedDependenciesSection sizeHint(Point sizeHint) {
		if (sizeHint != null) {
			this.sizeHint = sizeHint;
		}
		return this;
	}
}
