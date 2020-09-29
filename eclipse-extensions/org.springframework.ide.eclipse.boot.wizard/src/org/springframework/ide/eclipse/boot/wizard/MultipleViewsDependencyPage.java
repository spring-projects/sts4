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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.livexp.ui.DynamicSection;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.GroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

import com.google.common.collect.ImmutableList;

public  class MultipleViewsDependencyPage extends WizardPageWithSections {
	private static final int NUM_COLUMNS_FREQUENTLY_USED = 3;
	private static final int MAX_MOST_POPULAR = 3 * NUM_COLUMNS_FREQUENTLY_USED;
	private static final Point DEPENDENCY_SECTION_SIZE = new Point(SWT.DEFAULT, 300);

	private CheckBoxesSection<Dependency> frequentlyUsedCheckboxes;

	protected final InitializrFactoryModel<NewSpringBootWizardModel> factoryModel;

	protected MultipleViewsDependencyPage(InitializrFactoryModel<NewSpringBootWizardModel> factoryModel) {
		super("page2", "New Spring Starter Project Dependencies", null);
		this.factoryModel = factoryModel;
	}

	private void refreshFrequentlyUsedDependencies(NewSpringBootWizardModel model) {
		List<CheckBoxModel<Dependency>> dependenciesCheckboxes = model.getFrequentlyUsedDependencies(MAX_MOST_POPULAR);
		if (frequentlyUsedCheckboxes.isCreated()) {
			frequentlyUsedCheckboxes.setModel(dependenciesCheckboxes);
		}
		reflow();
	}

	@Override
	protected List<WizardPageSection> createSections() {
		DynamicSection dynamicSection = new DynamicSection(this, factoryModel.getModel().apply((dynamicModel) -> {
			if (dynamicModel != null) {
				return createDynamicSections(dynamicModel);
			}
			return new CommentSection(this, NewSpringBootWizard.NO_CONTENT_AVAILABLE);
		} ));

		return ImmutableList.of(dynamicSection);
	}

	protected WizardPageSection createDynamicSections(NewSpringBootWizardModel model) {
		List<WizardPageSection> sections = new ArrayList<>();

		RadioGroup bootVersion = model.getBootVersion();
		sections.add(
			new ChooseOneSectionCombo<>(this, bootVersion.getLabel(),
						bootVersion.getSelection(), bootVersion.getRadios()
			)
			.useFieldLabelWidthHint(false)
		);

		sections.add(createFrequentlyUsedSection(model));
		sections.add(createTwoColumnSection(model));
		return new GroupSection(this, null, sections.toArray(new WizardPageSection[0])).grabVertical(true);
	}

	public WizardPageSection createTwoColumnSection(final NewSpringBootWizardModel model) {
		return new GroupSection(this,null,
				new GroupSection(this, null,
						new CommentSection(this, "Available:"),
						getSearchSection(model),
						new GroupSection(this, "",
								new FilteredDependenciesSection(this, model, model.getDependencyFilter())
								.sizeHint(DEPENDENCY_SECTION_SIZE)
							)
							.grabVertical(true)
							.noMargins(true)
						)
						.grabVertical(true)
						.noMargins(true),
				new GroupSection(this, null,
						new CommentSection(this, "Selected:"),
						new GroupSection(this, "",
								new SelectedDependenciesSection(this, model)
								.sizeHint(DEPENDENCY_SECTION_SIZE)
							)
							.grabVertical(true)
							.noMargins(true),
						new MakeDefaultSection(this, () -> {
							if (model.saveDefaultDependencies()) {
								refreshFrequentlyUsedDependencies(model);
							}
						}, () -> {
							model.dependencies.clearSelection();
						})
					)
				)
				.columns(2, true)
				.grabVertical(true);
	}

	protected WizardPageSection getSearchSection(final NewSpringBootWizardModel model) {
		final SearchBoxSection searchBoxSection = new SearchBoxSection(this, model.getDependencyFilterBoxText()) {
			@Override
				protected String getSearchHint() {
					return "Type to search dependencies";
				}
		};
		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> getControl().addListener(SWT.Show, event -> searchBoxSection.focusControl()));
		return new GroupSection(this, null, searchBoxSection.grabFocus(true));
	}

	protected WizardPageSection createFrequentlyUsedSection(NewSpringBootWizardModel model) {
		List<CheckBoxModel<Dependency>> frequentDependencies = model.getFrequentlyUsedDependencies(MAX_MOST_POPULAR);
		frequentlyUsedCheckboxes = new CheckBoxesSection<>(this, frequentDependencies).columns(NUM_COLUMNS_FREQUENTLY_USED);
		GroupSection frequentlyUsedSection = new GroupSection(this,
				null,
				new CommentSection(this, "Frequently Used:"),
				new GroupSection(this, "", frequentlyUsedCheckboxes));
		frequentlyUsedSection.isVisible.setValue(!frequentDependencies.isEmpty());
		return frequentlyUsedSection;
	}

}
