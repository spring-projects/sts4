/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.springframework.tooling.boot.ls.BootLanguageServerPlugin;
import org.springframework.tooling.ls.eclipse.commons.Utils;

/**
 * Command for opening Java type given by its fully qualified name in an editor
 * 
 * @author Alex Boyko
 *
 */
public class OpenJavaElementInEditor extends AbstractHandler {
	
	private static final String BINDING_KEY = "bindingKey";
	private static final String PROJECT_NAME = "projectName";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String bindingKey = event.getParameter(BINDING_KEY);
		String projectName = event.getParameter(PROJECT_NAME);
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		
			if (project != null && bindingKey != null) {
				IJavaProject javaProject = JavaCore.create(project);
				if (javaProject != null) {
					IJavaElement element = Utils.findElement(javaProject, bindingKey);
					if (element == null) {
						BootLanguageServerPlugin.getDefault().getLog().log(new Status(IStatus.WARNING, BootLanguageServerPlugin.ID, "Cannot find element: " + bindingKey));
					}
					if (element instanceof IMember) {
						IMember member = (IMember) element;
						try {
							IEditorPart editorPart = JavaUI.openInEditor(member.getDeclaringType());
							JavaUI.revealInEditor(editorPart, (IJavaElement)member);
						} catch (PartInitException | JavaModelException e) {
							throw new ExecutionException("Error opening java element in editor", e);
						}
					}
				} else {
					BootLanguageServerPlugin.getDefault().getLog().log(new Status(IStatus.WARNING, BootLanguageServerPlugin.ID, "Cannot find project: " + projectName));
				}
			}

		return null;
	}
	
}
