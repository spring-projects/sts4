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
package org.springframework.ide.eclipse.boot.dash.cf.deployment;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.dialogs.ManifestDiffDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.ManifestDiffDialogModel.Result;

/**
 * Dialog to compare and merge manifest file with deployment properties from CF.
 * Hosts eclipse's compare and merge editor composite.
 *
 * @author Alex Boyko
 *
 */
public class ManifestDiffDialog extends TitleAreaDialog {

	protected final CompareEditorInput fCompareEditorInput;
	private String title = "Merge Manifest File";

	/**
	 * Create a dialog to host the given input.
	 * @param shell a shell
	 * @param input the dialog input
	 */
	public ManifestDiffDialog(Shell shell, ManifestDiffDialogModel model) {
		super(shell);
		CompareEditorInput input = model.getInput();
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		Assert.isNotNull(input);
		fCompareEditorInput= input;
	}

	public ManifestDiffDialog(Shell shell, ManifestDiffDialogModel model, String title) {
		this(shell, model);
		this.title = title;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.YES_ID, "Use Manifest", true);
		createButton(parent, IDialogConstants.CANCEL_ID, fCompareEditorInput.getCancelButtonLabel(), false);
		createButton(parent, IDialogConstants.NO_ID, "Forget Manifest", false);
	}

	protected Control createDialogArea(Composite parent2) {

		Composite parent= (Composite) super.createDialogArea(parent2);

		Control c= fCompareEditorInput.createContents(parent);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));

		Shell shell= c.getShell();
		shell.setText(fCompareEditorInput.getTitle());
		shell.setImage(fCompareEditorInput.getTitleImage());
		applyDialogFont(parent);
		return parent;
	}

	@Override
	public void create() {
		super.create();
		setTitle(title);
		if (fCompareEditorInput != null && fCompareEditorInput.getMessage() != null) {
			setMessage(fCompareEditorInput.getMessage(), IMessageProvider.WARNING);
		} else {
			setMessage(
					"Manifest file deployment properties are different from current deployment properties on CF. Please merge changes if applicable.",
					IMessageProvider.INFORMATION);
		}
	}

	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case IDialogConstants.NO_ID:
			fCompareEditorInput.cancelPressed();
			setReturnCode(buttonId);
			close();
			break;
		case IDialogConstants.YES_ID:
			if (fCompareEditorInput.isDirty()) {
				if (!fCompareEditorInput.okPressed())
					return;
			} else {
				fCompareEditorInput.cancelPressed();
			}
			setReturnCode(buttonId);
			close();
			break;
		case IDialogConstants.CANCEL_ID:
			fCompareEditorInput.cancelPressed();
			super.buttonPressed(buttonId);
			break;
		default:
			super.buttonPressed(buttonId);
		}
	}

	protected IDialogSettings getDialogBoundsSettings() {
		return DialogSettings.getOrCreateSection(BootDashActivator.getDefault().getDialogSettings(), "MergeManifestDialog");
	}

	/**
	 * Return the compare editor input for this dialog.
	 * @return the compare editor input for this dialog
	 */
	protected final CompareEditorInput getInput() {
		return fCompareEditorInput;
	}

	@Override
	protected int getDialogBoundsStrategy() {
		return DIALOG_PERSISTSIZE;
	}

	public static Result getResultForCode(int buttonId) {
		switch (buttonId) {
			case IDialogConstants.NO_ID:
				return Result.FORGET_MANIFEST;
			case IDialogConstants.YES_ID:
				return Result.USE_MANIFEST;
			case IDialogConstants.CANCEL_ID:
				return Result.CANCELED;
			default:
				throw new IllegalArgumentException("Unknown button ID");
		}
	}
}
