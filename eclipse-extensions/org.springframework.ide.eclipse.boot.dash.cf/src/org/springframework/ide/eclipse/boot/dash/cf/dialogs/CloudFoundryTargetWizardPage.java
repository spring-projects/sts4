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
package org.springframework.ide.eclipse.boot.dash.cf.dialogs;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCredentials.LoginMethod;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.editor.support.util.CollectionUtil;
import org.springsource.ide.eclipse.commons.livexp.core.CompositeValidator;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.CheckboxSection;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.UIConstants;
import org.springsource.ide.eclipse.commons.livexp.ui.ValidatorSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

/**
 * Creates a Cloud Foundry target by prompting user for credentials and Cloud
 * Foundry target URL.
 *
 *
 */
public class CloudFoundryTargetWizardPage extends WizardPageWithSections {

	private CloudFoundryTargetWizardModel model;

	private class SelectSpaceSection extends WizardPageSection {

		private CompositeValidator spaceSectionValidator = new CompositeValidator();
		{
			//Do these two really need to be exposed from the model as separate entities?
			//   I don't think they are really used separately?
			// Keeping it like this for now as it sort of make sense. The ui here has two pieces
			//  one is a box showing a selected space. And the other a button to resolve spaces.
			spaceSectionValidator.addChild(model.getResolvedSpacesValidator());
			spaceSectionValidator.addChild(model.getSpaceValidator());
		}

		public SelectSpaceSection(IPageWithSections owner) {
			super(owner);
		}

		@Override
		public void createContents(Composite page) {
	        Composite buttonComposite = new Composite(page, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonComposite);
	        GridLayout layout = GridLayoutFactory.fillDefaults().numColumns(3).margins(0,2).create();
	        buttonComposite.setLayout(layout);

	        Label label = new Label(buttonComposite, SWT.NONE);
	        label.setText("Space:");
	        GridDataFactory.fillDefaults()
	        	.hint(UIConstants.fieldLabelWidthHint(label), SWT.DEFAULT)
	        	.align(SWT.BEGINNING, SWT.CENTER)
	        	.applyTo(label);

			Text spaceValueText = new Text(buttonComposite, SWT.BORDER);
			spaceValueText.setEnabled(false);
			spaceValueText.setBackground(buttonComposite.getBackground());
			GridDataFactory.fillDefaults().grab(true, false).applyTo(spaceValueText);
			model.getSpaceVar().addListener((exp, value) -> {
				if (spaceValueText != null && !spaceValueText.isDisposed()) {
					spaceValueText.setText(value != null ? value.getName() : "");
				}
			});

			Button selectSpaceButton = new Button(buttonComposite, SWT.PUSH);
			selectSpaceButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
			selectSpaceButton.setText("Select Space...");
			selectSpaceButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {

					// Fetch an updated list of orgs and spaces in a cancellable
					// operation (i.e. the operation
					// can be cancelled in the wizard's progress bar)
					model.resolveSpaces(getWizard().getContainer());
					OrgsAndSpaces spaces = model.getSpaces();
					if (spaces != null && CollectionUtil.hasElements(spaces.getAllSpaces())) {
						OrgsAndSpacesWizard spacesWizard = new OrgsAndSpacesWizard(model);
						WizardDialog dialog = new WizardDialog(getShell(), spacesWizard);
						dialog.open();
					}
				}
			});

			//Enable the "Select Space" button once credentials are complete.
			model.getCredentialsValidator().addListener((exp, value) -> {
				if (selectSpaceButton != null && !selectSpaceButton.isDisposed()) {
					selectSpaceButton.setEnabled(value.isOk());
				}
			});
		}

		@Override
		public LiveExpression<ValidationResult> getValidator() {
			return spaceSectionValidator;
		}

	}


	private CloudFoundryRunTarget runTarget = null;

	public CloudFoundryTargetWizardPage(CloudFoundryTargetWizardModel model) {
		super("page1", "Add a Cloud Foundry Target", BootDashActivator.getImageDescriptor("icons/wizban_cloudfoundry.png"));
		this.model = model;
		setDescription("Enter credentials and a Cloud Foundry target URL.");
	}

	@Override
	protected List<WizardPageSection> createSections() {
		List<WizardPageSection> sections = new ArrayList<>();
		sections.add(new ChooseOneSectionCombo<>(this, "Method:", model.getMethodVar(), EnumSet.allOf(LoginMethod.class)));
		//TODO: hide password or passcode field depending on the method.
		sections.add(new StringFieldSection(this, "Email:", model.getUserNameVar())
				.setEnabler(model.getEnableUserName()));

		sections.add(new StringFieldSection(this, "Password:", model.getPasswordVar()).setPassword(true));
		sections.add(UpdatePasswordDialog.storeCredentialsSection(this, model.getStoreVar(), model.getStoreCredentialsValidator()));
		sections.add(new StringFieldSection(this, "Url:", model.getUrlVar()));
		sections.add(new ValidatorSection(model.getCredentialsValidator(), this));
		sections.add(new SelectSpaceSection(this));
		sections.add(new CheckboxSection(this, model.getSkipSslVar(), "Skip SSL Validation"));
		return sections;
	}

	/**
	 * Creates a run target ONCE.
	 * @return created run target. Returns cached target if already created.
	 */
	public CloudFoundryRunTarget createRunTarget() {
		// Cache to avoid creating run target multiple times in the same wizard session
		if (runTarget == null) {
			try {
				runTarget = model.finish();
			} catch (Exception e) {
				setErrorMessage(e.getMessage());
			}
		}
		return runTarget;
	}

}
