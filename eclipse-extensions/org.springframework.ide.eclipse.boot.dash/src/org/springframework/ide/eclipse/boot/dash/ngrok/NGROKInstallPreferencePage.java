/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.ngrok;

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
import org.springframework.ide.eclipse.boot.util.SWTFactory;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;

/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Kris De Volder
 */
public class NGROKInstallPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String NGROK_DIALOGSETTINGS = "org.springframework.ide.eclipse.boot.dash.ngrok.dialogsettings";

	private NGROKInstallBlock ngrokBlock;
	private NGROKInstallManager installManager;

	public NGROKInstallPreferencePage() {
		super("ngrok installation");
	}

	public void init(IWorkbench workbench) {
		try {
			installManager = NGROKInstallManager.getInstance();
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	@Override
	public boolean isValid() {
//		if (super.isValid()) {
//			if (getCurrentDefaultVM() == null && fJREBlock.getJREs().length > 0) {
//				setErrorMessage("Select a default Boot installation");
//				return false;
//			}
//		}
		return true;
	}

	private String getCurrentDefaultNGROK() {
		return ngrokBlock.getCheckedNGROK();
	}

	@Override
	public boolean performOk() {
		final boolean[] canceled = new boolean[] { false };
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				Set<String> newInstalls = new LinkedHashSet<String>();
				String defaultNgrok = getCurrentDefaultNGROK();
				String[] ngroks = ngrokBlock.getNGROKs();
				for (String ngrok : ngroks) {
					try {
						newInstalls.add(ngrok);
					} catch (Exception e) {
						BootActivator.log(e);
					}
				}

				installManager.setDefaultInstall(defaultNgrok);
				installManager.setInstalls(newInstalls);
				installManager.save();
			}
		});

		if (canceled[0]) {
			return false;
		}

		// save column widths
		IDialogSettings settings = BootActivator.getDefault().getDialogSettings();
		ngrokBlock.saveColumnSettings(settings, NGROK_DIALOGSETTINGS);

		return super.performOk();
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
						"Add, edit or remove ngrok installations. By default the checked ngrok installation will be used to expose apps",
						1, 300);
		SWTFactory.createVerticalSpacer(ancestor, 1);

		ngrokBlock = new NGROKInstallBlock(installManager);
		ngrokBlock.createControl(ancestor);
		ngrokBlock.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				isValid();
			}
		});

		Control control = ngrokBlock.getControl();
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 1;
		control.setLayoutData(data);

		ngrokBlock.restoreColumnSettings(BootActivator.getDefault().getDialogSettings(),
				NGROK_DIALOGSETTINGS);

		ngrokBlock.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				setValid(false);

				String install = getCurrentDefaultNGROK();
				if (install == null) {
					setErrorMessage("Select a default ngrok installation");
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
