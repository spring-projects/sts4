/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.plugins;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nieraj Singh
 * @author Andy Clement
 * @author Christian Dupuis
 * @author Kris De Volder
 */
public abstract class CommandDialog extends TitleAreaDialog {

	private List<IProject> projects;


	public CommandDialog(Shell parentShell, List<IProject> projects2) {
		super(parentShell);
		this.projects = projects2;
	}

	protected List<IProject> getProjects() {
		return projects;
	}


	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(getTitle());
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		setTitle(getTitle());
		setMessage(getMessage());
		// FIXME setTitleImage(GrailsUiActivator.getImageDescriptor("icons/full/wizban/grails_wizban.png").createImage());

		Composite composite = new Composite(parent, SWT.NONE);

		GridLayoutFactory.fillDefaults().margins(getDefaultCompositeHMargin(), getDefaultCompositeVMargin()).spacing(
				convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING),
				convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING)).applyTo(composite);

		Dialog.applyDialogFont(composite);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		createCommandArea(composite);

		return composite;
	}

	protected int getDefaultCompositeVMargin() {
		return convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
	}

	protected int getDefaultCompositeHMargin() {
		return convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
	}

	public String getMessage() {
		return "";
	}

	public String getTitle() {
		return "";
	}

	abstract protected void createCommandArea(Composite parent);

}
