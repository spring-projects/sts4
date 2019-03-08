/*******************************************************************************
 * Copyright (c) 2000, 2009, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Kris De Volder - copied from org.eclipse.debug.internal.ui.views.console.ProcessConsolePageParticipant
 *                    - modified to create similar functionality for GrailsIOConsole
 *     Kris De Volder - copied and modified to support lsp console close action
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons.console;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.part.IPageBookViewPage;

public class LanguageServerConsolePageParticipant implements IConsolePageParticipant {

	// actions
	private CloseConsoleAction fClose;

	private IPageBookViewPage fPage;

	private IConsoleView fView;

	private LanguageServerIOConsole fConsole;

	@Override
	public void init(IPageBookViewPage page, IConsole console) {
		fPage = page;
		fConsole = (LanguageServerIOConsole) console;

		fClose = new CloseConsoleAction(page.getSite().getWorkbenchWindow(), fConsole);

		fView = (IConsoleView) fPage.getSite().getPage().findView(IConsoleConstants.ID_CONSOLE_VIEW);

		// contribute to toolbar
		IActionBars actionBars = fPage.getSite().getActionBars();
		configureToolBar(actionBars.getToolBarManager());
	}

	@Override
	public <T> T getAdapter(Class<T> arg0) {
		return null;
	}

	@Override
	public void dispose() {
		if (fClose != null) {
			fClose.dispose();
			fClose = null;
		}
		//		if (fStdOut != null) {
		//			fStdOut.dispose();
		//			fStdOut = null;
		//		}
		//		if (fStdErr != null) {
		//			fStdErr.dispose();
		//			fStdErr = null;
		//		}
		fConsole = null;
	}

	/**
	 * Contribute actions to the toolbar
	 */
	protected void configureToolBar(IToolBarManager mgr) {
		mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fClose);

		//      mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fRemoveTerminated);
		//		mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fRemoveAllTerminated);
		//		mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fStdOut);
		//		mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fStdErr);
	}

	@Override
	public void activated() {
		// add EOF submissions
		//        IPageSite site = fPage.getSite();
		//        IHandlerService handlerService = (IHandlerService)site.getService(IHandlerService.class);
		//        IContextService contextService = (IContextService)site.getService(IContextService.class);
		//        fActivatedContext = contextService.activateContext(fContextId);
		//        fActivatedHandler = handlerService.activateHandler("org.eclipse.debug.ui.commands.eof", fEOFHandler); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsolePageParticipant#deactivated()
	 */
	@Override
	public void deactivated() {
		// remove EOF submissions
		//        IPageSite site = fPage.getSite();
		//        IHandlerService handlerService = (IHandlerService)site.getService(IHandlerService.class);
		//        IContextService contextService = (IContextService)site.getService(IContextService.class);
		//      handlerService.deactivateHandler(fActivatedHandler);
		//		contextService.deactivateContext(fActivatedContext);
	}

}