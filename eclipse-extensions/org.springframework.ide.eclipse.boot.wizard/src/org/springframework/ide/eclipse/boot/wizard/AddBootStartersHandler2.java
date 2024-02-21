/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersWizard;

public class AddBootStartersHandler2 extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IEditorInput input = HandlerUtil.getActiveEditorInput(event);
			if (input instanceof IFileEditorInput fileInput) {
				AddStartersWizard.openFor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), fileInput.getFile().getProject());
				return null;
			}
		} catch (Exception e) {
			throw new ExecutionException("", e);
		}
		throw new ExecutionException("Current active editor is not for pom.xml");
	}

}
