/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons.console;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.springframework.tooling.ls.eclipse.commons.LanguageServerCommonsActivator;

public class CloseConsoleAction extends Action {

	private LanguageServerIOConsole fConsole;

	public CloseConsoleAction(IWorkbenchWindow workbenchWindow, LanguageServerIOConsole fConsole) {
		super("Close Console", LanguageServerCommonsActivator.getImageDescriptor("icons/rem_co.png"));
		this.fConsole = fConsole;
	}

	public void dispose() {
		//TODO: anything to clean up?
	}

	@Override
	public void run() {
		ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[]{fConsole});
	}

}
