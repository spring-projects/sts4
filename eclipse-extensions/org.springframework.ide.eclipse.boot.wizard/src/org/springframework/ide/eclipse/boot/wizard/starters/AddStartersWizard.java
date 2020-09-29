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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.wizard.StartersWizardUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class AddStartersWizard extends Wizard implements IWorkbenchWizard {

	private AddStartersWizardModel wizardModel;
	private CompareGeneratedAndCurrentPage comparePage;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		IProject project = StartersWizardUtil.getProject(selection);
		init(workbench, project);
	}

	public void init(IWorkbench workbench, IProject project) {
		try {
			if (project != null) {
				IPreferenceStore preferenceStore = BootActivator.getDefault().getPreferenceStore();
				URLConnectionFactory urlConnectionFactory = BootActivator.getUrlConnectionFactory(conn -> conn.addRequestProperty("X-STS-ACTION", "diff"));
				AddStartersPreferences preferences = new AddStartersPreferences(preferenceStore);
				AddStartersInitializrService initializrService = new AddStartersInitializrService(urlConnectionFactory);
				wizardModel = new AddStartersWizardModel(project, preferences, initializrService);
			}
		} catch (Exception e) {
			MessageDialog.openError(workbench.getActiveWorkbenchWindow().getShell(), "Error opening the wizard",
					ExceptionUtil.getMessage(e)+"\n\n"+
					"Note that this wizard uses a webservice and needs internet access.\n"+
					"A more detailed error message may be found in the Eclipse error log."
			);
			//Ensure exception is logged. (Eclipse UI may not log it!).
			Log.log(e);
			throw new Error(e);
		}
	}

	@Override
	public boolean canFinish() {
		// TODO: can this logic be moved to the wizard model?
		// Essential the wizard cannot finish unless the compare page is open and complete
		if (this.comparePage != null) {
			return this.comparePage.isPageComplete();
		}
		return super.canFinish();
	}

	@Override
	public boolean performFinish() {
		 wizardModel.performOk();
		 return true;
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(new DependencyPage(wizardModel));
		this.comparePage = new CompareGeneratedAndCurrentPage(wizardModel);
		addPage(comparePage);
	}

	public static void openFor(Shell shell, IStructuredSelection selection) throws CoreException {
		IProject project = StartersWizardUtil.getProject(selection);
		openFor(shell, project);
	}

	public static void openFor(Shell shell, IProject project) throws CoreException {
		if (project != null) {
			int promptResult = StartersWizardUtil.promptIfPomFileDirty(project, shell);
			if (promptResult == MessageDialog.OK) {
				AddStartersWizard addStartersWizard = new AddStartersWizard();
				addStartersWizard.init(PlatformUI.getWorkbench(), project);
				new WizardDialog(shell, addStartersWizard) {

					@Override
					public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable)
							throws InvocationTargetException, InterruptedException {
						super.run(fork, cancelable, runnable);
						// The above restores the UI state to what it was before executing the runnable
						// If after the execution UI state updates (i.e. buttons enabled) restore state
						// following the execution of the runnable would wipe out the correct UI state.
						// Therefore update the UI after the execution to minimize the effect of
						// restoreUiState
						updateButtons();
					}

				}.open();
			}
		}
	}

	@Override
	public boolean needsProgressMonitor() {
		return true;
	}
}
