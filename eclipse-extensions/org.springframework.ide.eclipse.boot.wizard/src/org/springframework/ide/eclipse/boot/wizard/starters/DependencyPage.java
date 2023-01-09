/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.livexp.ui.DynamicSection;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springframework.ide.eclipse.boot.wizard.FilteredDependenciesSection;
import org.springframework.ide.eclipse.boot.wizard.MakeDefaultSection;
import org.springframework.ide.eclipse.boot.wizard.SearchBoxSection;
import org.springframework.ide.eclipse.boot.wizard.SelectedDependenciesSection;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.GroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.LabeledPropertySection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;
import org.springsource.ide.eclipse.commons.livexp.util.Parser;

public class DependencyPage extends WizardPageWithSections {

	private static final int NUM_COLUMNS_FREQUENTLY_USED = 3;
	private static final int MAX_MOST_POPULAR = 3 * NUM_COLUMNS_FREQUENTLY_USED;
	private static final Point DEPENDENCY_SECTION_SIZE = new Point(SWT.DEFAULT, 300);

	private CheckBoxesSection<Dependency> frequentlyUsedCheckboxes;


	protected final AddStartersWizardModel wizardModel;

	private ErrorGroupSection errorSection;

	/**
	 *
	 * Creates the dynamic sections of this wizard based on the wizard model's availability
	 * and validation
	 *
	 */

	final private LiveExpression<IPageSection> dynamicControlCreation = new LiveExpression<IPageSection>() {

		@Override
		protected IPageSection compute() {
			List<WizardPageSection> sections = new ArrayList<>();
			// If model is available, model loading has been successful
			InitializrModel model = wizardModel.getInitializrModel().getValue();
			ValidationResult validation = wizardModel.getValidator().getValue();
			if (validation != null && validation.status ==  IStatus.ERROR) {
				syncInUi(() -> {
					if (DependencyPage.this.errorSection != null)  {
						DependencyPage.this.errorSection.setDetailsAndShow(validation);
					}
				});
			}
			else if (model != null) {
				syncInUi(() -> {
					if (DependencyPage.this.errorSection != null)  {
						DependencyPage.this.errorSection.hide();
					}
				});

				model.onDependencyChange(() -> {
					asyncInUi(() -> {
						refreshWizardUi();
					});
				});
				createDynamicSections(model, sections);
			}

			GroupSection groupSection = new GroupSection(DependencyPage.this, null, 1,
					sections.toArray(new WizardPageSection[0]));
			groupSection.grabVertical(true);
			groupSection.setFocus();
			return groupSection;
		}
	};

	protected void syncInUi(Runnable runnable) {
		Display.getDefault().syncExec(() -> {
			runnable.run();
		});
	}

	protected void asyncInUi(Runnable runnable) {
		Display.getDefault().asyncExec(() -> {
			runnable.run();
		});
	}

	public DependencyPage(AddStartersWizardModel wizardModel) {
		super("Dependencies", "New Spring Starter Project Dependencies", null);
		this.wizardModel = wizardModel;
		// Add the model loader before UI is created, so that it can be invoked as UI is created (e.g. URL field is created)
		wizardModel.addModelLoader(() -> {
			runWithWizardProgress(monitor -> {
				monitor.beginTask("Loading starters data", IProgressMonitor.UNKNOWN);
				monitor.subTask("Creating Boot project model and fetching data from Initializr Service...");
				wizardModel.createInitializrModel(monitor);
				monitor.done();
			});
		});
	}

	private void refreshFrequentlyUsedDependencies(InitializrModel model) {
		List<CheckBoxModel<Dependency>> dependenciesCheckboxes = model.getFrequentlyUsedDependencies(MAX_MOST_POPULAR);
		if (frequentlyUsedCheckboxes.isCreated()) {
			frequentlyUsedCheckboxes.setModel(dependenciesCheckboxes);
		}
		reflow();
	}

	@Override
	protected List<WizardPageSection> createSections() {
		// "link" the page section creation to the wizard model. When the model gets validated,
		// it will trigger dynamic page creation
		dynamicControlCreation.dependsOn(wizardModel.getValidator());
		List<WizardPageSection> sections = new ArrayList<>();

		// PT 172323896 - Focus is lost in the Service URL control every time
		// a user types a new character and the model is reloaded.
		// The reason is that, although the wizard dialog's built-in mechanism to
		// restore focus on controls works (the dialog correctly remembers that the service URL
		// control had focus PRIOR to starting background progress work, like model loading, and
		// attempts to restore it after work is completed - see org.eclipse.jface.wizard.WizardDialog#stopped())
		// this automatic restoration of focus on a control will NOT work if the control is disposed
		// which would happen for dynamically created sections, which get recreated on each model loading.
		// SOLUTION: EXCLUDE "static" controls like the service URL and boot version
		// from being recreated every time (i.e. dont include their creation in the dynamic section).
		// This ensures that when the wizard attempts to restore focus on the service URL control, it is
		// still active and not disposed
		createBootInfoSection(sections);
		createErrorSection(sections);

		DynamicSection dynamicSection = new DynamicSection(this, dynamicControlCreation);
		sections.add(dynamicSection);

		return sections;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		// Add any validators to the wizard validator. This is important
		// as this "hooks" the separate validators to the wizards general validation
		// mechanism, which among things is responsbile for showing errors from
		// the various validators that exist
		validator.addChild(wizardModel.getValidator());
	}

	private void createErrorSection(List<WizardPageSection> sections) {
		errorSection = new ErrorGroupSection(this);
		sections.add(errorSection.getGroupSection());
		// Hide initially until validation dictates when to show the section
		errorSection.hide();
	}


	private void runWithWizardProgress(IRunnableWithProgress runnable) {
		getWizard().getContainer().getShell().getDisplay().asyncExec(() -> {
			try {
				getContainer().run(true, false, runnable);
			} catch (Exception e) {
				String message = ExceptionUtil.getMessage(e);
				setErrorMessage(message);
				Log.log(e);
			} finally {
				// After loading of starters data is completed set the focus to the search box.
				// It is highly unlikely that use had the time to switch focus to anything else.
				if (dynamicControlCreation.getValue() != null) {
					dynamicControlCreation.getValue().setFocus();
				}
			}
		});
	}

	protected void createDynamicSections(InitializrModel model, List<WizardPageSection> sections) {
		sections.add(createFrequentlyUsedSection(model));
		sections.add(createTwoColumnSection(model));
	}

	@SuppressWarnings("resource")
	public WizardPageSection createTwoColumnSection(final InitializrModel model) {
		return new GroupSection(this, null, 0,
				new GroupSection(this, null, 1,
						new CommentSection(this, "Available:"),
						getSearchSection(model),
						new GroupSection(this, "",
								new FilteredDependenciesSection(this, model.dependencies, model.searchBox.getFilter())
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
								new SelectedDependenciesSection(this, model.dependencies)
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

	protected WizardPageSection getSearchSection(final InitializrModel model) {
		return new SearchBoxSection(this, model.searchBox.getText()) {
			@Override
			protected String getSearchHint() {
				return "Type to search dependencies";
			}

		};
	}

	@SuppressWarnings("resource")
	protected WizardPageSection createFrequentlyUsedSection(InitializrModel model) {
		List<CheckBoxModel<Dependency>> frequentDependencies = model.getFrequentlyUsedDependencies(MAX_MOST_POPULAR);
		frequentlyUsedCheckboxes = new CheckBoxesSection<>(this, frequentDependencies).columns(NUM_COLUMNS_FREQUENTLY_USED);
		GroupSection frequentlyUsedSection = new GroupSection(this,
				null,
				new CommentSection(this, "Frequently Used:"),
				new GroupSection(this, "", frequentlyUsedCheckboxes));
		frequentlyUsedSection.isVisible.setValue(!frequentDependencies.isEmpty());
		return frequentlyUsedSection;
	}

	protected void createBootInfoSection(List<WizardPageSection> sections) {
		ChooseOneSectionCombo<String> serviceUrlSection  = new ChooseOneSectionCombo<String>(this, wizardModel.getServiceUrl(), wizardModel.getServiceUrlOptions())
				.grabHorizontal(true)
				.showErrorMarker(true);
		serviceUrlSection.allowTextEdits(Parser.IDENTITY);

		sections.add(serviceUrlSection);

		LabeledPropertySection section = new LabeledPropertySection(this, wizardModel.getBootVersion());
		sections.add(section);
	}

	@Override
	public boolean isPageComplete() {
		// We cannot complete from  the dependency page as
		// a user has to go to the  next page to manually accept changes
		// into their existing project
		return false;
	}

	@Override
	public boolean canFlipToNextPage() {
		return isValid();
	}

	private boolean isValid() {
		LiveExpression<ValidationResult> wizardModelValidator = wizardModel.getValidator();
		return wizardModelValidator.getValue() != null && wizardModelValidator.getValue().isOk();
	}

	private void refreshWizardUi() {
		IWizard wizard = DependencyPage.this.getWizard();
		if (wizard != null) {
			IWizardContainer container = wizard.getContainer();
			if (container != null) {
				container.updateButtons();
			}
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			// Restore focus to search box when going back a page. There is no control that has the focus going back hence set it.
			if (dynamicControlCreation.getValue() != null) {
				dynamicControlCreation.getValue().setFocus();
			}
		}
	}

	private static class ErrorGroupSection {

		private static final String NO_CONTENT = "No content available";
		private final GroupSection section;
		private final CommentSection comments;

		public ErrorGroupSection(IPageWithSections owner) {
			comments = new CommentSection(owner, NO_CONTENT);
			section = new GroupSection(owner, null, new CommentSection(owner, "Details:"),
					new GroupSection(owner, "", comments));
		}

		public GroupSection getGroupSection() {
			return this.section;
		}

		public void setDetailsAndShow(ValidationResult results) {
			if (results instanceof AddStartersError) {
				String details = ((AddStartersError) results).details;
				comments.setText(details);
			} else {
				comments.setText(NO_CONTENT);
			}
			section.isVisible.setValue(true);
		}

		public void hide() {
			section.isVisible.setValue(false);
		}

	}
}
