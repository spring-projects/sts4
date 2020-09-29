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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 *
 */
public class SelectMainTypeWizardPage extends WizardPage {

	private Combo typeCombo;

	boolean canFinish = false;

	private final List<IType> mainTypes;

	private IType selectedMainType;

	protected SelectMainTypeWizardPage(List<IType> mainTypes,
			ImageDescriptor descriptor) {
		super("Select Java Main Type");
		this.mainTypes = mainTypes;
		setTitle("Select Java Main Type");
		setDescription("Multiple Java main types found. Please select one.");

		if (descriptor != null) {
			setImageDescriptor(descriptor);
		}
	}

	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(10, 10)
				.applyTo(composite);

		Label mainTypeLabel = new Label(composite, SWT.NONE);
		mainTypeLabel.setText("Main type:");
		GridDataFactory.fillDefaults().applyTo(mainTypeLabel);

		typeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);

		GridDataFactory.fillDefaults().applyTo(typeCombo);

		typeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resolveSelection();
			}
		});

		String[] comboItems = new String[mainTypes.size()];
		for (int i = 0; i < comboItems.length && i < mainTypes.size(); i++) {
			String name = mainTypes.get(i).getFullyQualifiedName();
			comboItems[i] = name;
		}

		typeCombo.setItems(comboItems);
		if (mainTypes.size() > 0) {
			typeCombo.select(0);
		}
		resolveSelection();
		Dialog.applyDialogFont(composite);
		setControl(composite);

	}

	private void resolveSelection() {
		if (typeCombo != null && !typeCombo.isDisposed()) {
			int index = typeCombo.getSelectionIndex();

			if (index >= 0) {
				String typeName = typeCombo.getItem(index);
				for (IType type : mainTypes) {
					if (type.getFullyQualifiedName().equals(typeName)) {
						selectedMainType = type;
						break;
					}
				}
			}
			update();
		}
	}

	private void update() {

		setErrorMessage(null);
		canFinish = getSelectedMainType() != null;

		if (getWizard() != null && getWizard().getContainer() != null) {
			getWizard().getContainer().updateButtons();
			setPageComplete(canFinish);
		}
	}

	@Override
	public boolean isPageComplete() {
		return canFinish;
	}

	public IType getSelectedMainType() {
		return selectedMainType;
	}
}
