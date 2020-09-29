/*******************************************************************************
 *  Copyright (c) 2012,2013 Pivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui.preferences;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.cli.BootInstallManager;
import org.springframework.ide.eclipse.boot.core.cli.install.IBootInstall;
import org.springframework.ide.eclipse.boot.util.SWTFactory;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;


/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Kris De Volder
 */
public class BootInstallPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private InstalledBootInstallBlock fJREBlock;
	private BootInstallManager installManager;

	public BootInstallPreferencePage() {
		super("Spring Boot Installations");
	}

	@Override
	public void init(IWorkbench workbench) {
		try {
			installManager = BootInstallManager.getInstance();
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	@Override
	public boolean isValid() {
		if (super.isValid()) {
			if (getCurrentDefaultVM() == null && fJREBlock.getJREs().length > 0) {
				setErrorMessage("Select a default Boot installation");
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean performOk() {
		final boolean[] canceled = new boolean[] { false };
		BusyIndicator.showWhile(null, new Runnable() {
			@Override
			public void run() {
				Set<IBootInstall> newInstalls = new LinkedHashSet<IBootInstall>();
				IBootInstall defaultVM = getCurrentDefaultVM();
				IBootInstall[] vms = fJREBlock.getJREs();
				for (IBootInstall vm : vms) {
					try {
						newInstalls.add(vm);
					} catch (Exception e) {
						BootActivator.log(e);
					}
				}

				installManager.setDefaultInstall(defaultVM);
				installManager.setInstalls(newInstalls);
				installManager.save();
			}
		});

		if (canceled[0]) {
			return false;
		}

		// save column widths
		IDialogSettings settings = BootActivator.getDefault().getDialogSettings();
		fJREBlock.saveColumnSettings(settings, "com.springsource.sts.boot.ui.dialogsettings");

		return super.performOk();
	}

	private IBootInstall getCurrentDefaultVM() {
		return fJREBlock.getCheckedJRE();
	}

	@Override
	protected Control createContents(Composite ancestor) {
		initializeDialogUnits(ancestor);

		noDefaultAndApplyButton();

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		ancestor.setLayout(layout);

		SWTFactory
				.createWrapLabel(
						ancestor,
						"Add, edit or remove Boot installations. By default the checked Boot installation will be used to launch 'app.groovy' Spring Boot CLI scripts",
						1, 300);
		SWTFactory.createVerticalSpacer(ancestor, 1);

		fJREBlock = new InstalledBootInstallBlock(installManager);
		fJREBlock.createControl(ancestor);
		fJREBlock.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				isValid();
			}
		});

		Control control = fJREBlock.getControl();
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 1;
		control.setLayoutData(data);

		fJREBlock.restoreColumnSettings(BootActivator.getDefault().getDialogSettings(),
				"com.springsource.sts.boot.ui.dialogsettings");

		fJREBlock.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setValid(false);

				IBootInstall install = getCurrentDefaultVM();
				if (install == null) {
					setErrorMessage("Select a default Boot installation");
				}
				else {
					setErrorMessage(null);
					setValid(true);
				}
			}
		});
		applyDialogFont(ancestor);
		return ancestor;
	}
}
