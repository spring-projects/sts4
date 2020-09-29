/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.console;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.part.IPageBookViewPage;

public class ConsolePageParticipant implements IConsolePageParticipant {

	private RemoveConsoleAction removeConsoleAction;

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public void init(IPageBookViewPage page, IConsole console) {
		IConsoleView view = (IConsoleView) page.getSite().getPage().findView(IConsoleConstants.ID_CONSOLE_VIEW);
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		removeConsoleAction = new RemoveConsoleAction(view, consoleManager);
		IActionBars actionBars = page.getSite().getActionBars();
		configureToolBar(actionBars.getToolBarManager());
	}

	private void configureToolBar(IToolBarManager toolBarManager) {
		toolBarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, removeConsoleAction);
	}

	@Override
	public void dispose() {
	}

	@Override
	public void activated() {
	}

	@Override
	public void deactivated() {
	}

}
