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

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersCompareModel.AddStartersTrackerState;
import org.springframework.ide.eclipse.boot.wizard.starters.eclipse.ResourceCompareInput;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class CompareGeneratedAndCurrentPage extends WizardPage {

	private final AddStartersWizardModel wizardModel;
	private Composite contentsContainer;
	private Control compareViewer = null;

	public CompareGeneratedAndCurrentPage(AddStartersWizardModel wizardModel) {
		super("Compare", "Compare local project with generated project from Spring Initializr", null);
		this.wizardModel = wizardModel;
	}

	@Override
	public void createControl(Composite parent) {
		contentsContainer = new Composite(parent, SWT.NONE);
		contentsContainer.setLayout(GridLayoutFactory.fillDefaults().create());
		setControl(contentsContainer);
	}

	@Override
	public boolean isPageComplete() {
		boolean isComplete = getWizard().getContainer().getCurrentPage() == this;
		AddStartersCompareModel compareModel = this.wizardModel.getCompareModel().getValue();
		boolean isDirty = false;
		if (compareModel != null) {
			ResourceCompareInput compareEditorInput = compareModel.getCompareEditorInput().getValue();
			isDirty = compareEditorInput != null && compareEditorInput.isDirty();
		}
		// Only enable "Finish" if there are changes to save
		return isComplete && isDirty;
	}

	@Override
	public void setVisible(boolean visible) {
		// Connect the model to the UI only when the page becomes visible.
		// If this connection is done before, either the UI controls may not yet be
		// created
		// or the model may not yet be available.

		if (visible) {
			connectToCompareModel();
		} else {
			disconnectFromCompareModel();
		}
		super.setVisible(visible);
	}

	private void generateComparison() {
		// Generate the comparison in the model and then do the "UI bits" (e.g. setting messages, creating the
		// viewer..)
		runInWizardContainer(monitor -> {

			AddStartersCompareModel compareModel = wizardModel.getCompareModel().getValue();
			compareModel.generateComparison(monitor);

			ResourceCompareInput input = compareModel.getCompareEditorInput().getValue();

			if (input != null) {
				runInUi(() -> {

					// PT 174276041 - Only enable Finish button if there are changes to save
					input.addPropertyChangeListener(new IPropertyChangeListener() {

						@Override
						public void propertyChange(PropertyChangeEvent event) {
							if (event != null && "DIRTY_STATE".equals(event.getProperty())) {
								getContainer().updateButtons();
							}
						}
					});

					try {
						if (input.hasDiffs()) {
							setMessage("Differences detected between local project and project from Initializr Service",
									IMessageProvider.INFORMATION);
						} else {
							setMessage("No differences found between local project and project from Initializr Service",
									IMessageProvider.WARNING);
						}

						compareViewer = input.createContents(contentsContainer);
						compareViewer.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
						contentsContainer.layout();
					} catch (Exception e) {
						setErrorMessage(
								"Error occurred: " + ExceptionUtil.getMessage(e));
						Log.log(e);
					}
				});
			}
		});
	}

	private void disconnectFromCompareModel() {
		AddStartersCompareModel model = wizardModel.getCompareModel().getValue();
		model.disposeTrackers();
		if (compareViewer != null) {
			compareViewer.dispose();
		}
	}

	private void connectToCompareModel() {
		AddStartersCompareModel model = wizardModel.getCompareModel().getValue();

		// Listener that wires into the wizard's messages
		ValueListener<AddStartersTrackerState> downloadStateListener = new ValueListener<AddStartersTrackerState>() {
			@Override
			public void gotValue(LiveExpression<AddStartersTrackerState> exp, AddStartersTrackerState downloadState) {
				runInUi(() -> {
					if (downloadState != null) {
						setMessage(downloadState.getMessage());
					} else {
						setMessage("");
					}
				});
			}
		};
		model.initTrackers();
		model.getStateTracker().onChange(downloadStateListener);

		generateComparison();
	}

	private void runInWizardContainer(IRunnableWithProgress runnable) {
		try {
			getWizard().getContainer().run(true, false, runnable);
		} catch (Exception ex) {
			setErrorMessage(
					"Failed to compare local project contents with generated project from the Initializr Service: " + ExceptionUtil.getMessage(ex));
			Log.log(ex);
		}
	}

	private void runInUi(Runnable runnable) {
		Display display = getWizard().getContainer().getShell().getDisplay();
		display.asyncExec(runnable);
	}

	@Override
	public void dispose() {
		super.dispose();
		wizardModel.dispose();
	}
}
