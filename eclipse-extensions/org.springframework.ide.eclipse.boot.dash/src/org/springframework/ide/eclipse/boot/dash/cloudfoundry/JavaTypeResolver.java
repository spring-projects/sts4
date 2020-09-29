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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.dash.util.UiUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.maintype.MainTypeFinder;

/**
 *
 * Helper methods for UI components that require Java type searching, given a
 * valid java project.
 */
public class JavaTypeResolver {

	private final IJavaProject project;

	public JavaTypeResolver(IJavaProject project) {
		this.project = project;
	}

	protected IJavaProject getJavaProject() {
		return project;
	}

	public IType[] getMainTypes(IProgressMonitor monitor) throws Exception {
		return MainTypeFinder.guessMainTypes(getJavaProject(), monitor);

	}

	public IType getMainTypesFromSource(IProgressMonitor monitor) throws Exception {
		if (project != null) {
			IType[] types = getMainTypes(monitor);
			// Enable when dependency to
			// org.springsource.ide.eclipse.commons.core is
			// added. This should be the common way to obtain main types
			// MainTypeFinder.guessMainTypes(project, monitor);

			if (types != null && types.length > 0) {

				final List<IType> typesFromSource = new ArrayList<IType>();

				for (IType type : types) {
					if (!type.isBinary() && !typesFromSource.contains(type)) {
						typesFromSource.add(type);
					}
				}

				if (typesFromSource.size() == 1) {
					return typesFromSource.get(0);
				} else if (typesFromSource.size() > 1) {
					// Prompt user to select a main type

					final IType[] selectedType = new IType[1];
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {

							final Shell shell = UiUtil.getShell();

							if (shell != null && !shell.isDisposed()) {
								SelectMainTypeWizard wizard = new SelectMainTypeWizard(typesFromSource);
								WizardDialog dialog = new WizardDialog(shell, wizard);
								if (dialog.open() == Window.OK) {
									selectedType[0] = wizard.getSelectedMainType();
								}

							} else {
								selectedType[0] = typesFromSource.get(0);
							}
						}
					});
					return selectedType[0];
				}
			}
		}
		return null;
	}
}
