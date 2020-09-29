/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Phil Webb - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.restart;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;

@SuppressWarnings("restriction")
public class RestartConsolePageParticipant implements IConsolePageParticipant,
		IDebugEventSetListener {

	private static final String DEBUG_CONTEXT_ID = "org.eclipse.debug.ui.console"; //$NON-NLS-1$;

	// See ProcessConsolePageParticipant

	private IPageBookViewPage page;

	private ProcessConsole console;

	private RestartAction restart;

	private IContextActivation activatedContext;

	private IHandlerActivation activatedHandler;

	private RestartHandler restartHandler;

	public void init(IPageBookViewPage page, IConsole console) {
		init(page, (ProcessConsole) console);
	}

	private void init(IPageBookViewPage page, ProcessConsole console) {
		this.restart = new RestartAction(console);
		this.page = page;
		this.console = console;
		this.restartHandler = new RestartHandler();
		DebugPlugin.getDefault().addDebugEventListener(this);
		configureToolBar(page.getSite().getActionBars().getToolBarManager());
	}

	private void configureToolBar(IToolBarManager manager) {
		manager.prependToGroup(IConsoleConstants.LAUNCH_GROUP, this.restart);
	}

	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		if (this.restart != null) {
			this.restart.dispose();
			this.restart = null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		return null;
	}

	public void activated() {
		IPageSite site = this.page.getSite();
		if (this.activatedContext == null && this.activatedHandler == null) {
			IHandlerService handlerService = (IHandlerService) site
					.getService(IHandlerService.class);
			IContextService contextService = (IContextService) site
					.getService(IContextService.class);
			this.activatedContext = contextService.activateContext(DEBUG_CONTEXT_ID);
			this.activatedHandler = handlerService
					.activateHandler("org.springframework.ide.eclipse.boot.restart."
							+ "commands.restart", this.restartHandler); //$NON-NLS-1$
		}

	}

	public void deactivated() {
		IPageSite site = this.page.getSite();
		IHandlerService handlerService = (IHandlerService) site
				.getService(IHandlerService.class);
		IContextService contextService = (IContextService) site
				.getService(IContextService.class);
		handlerService.deactivateHandler(this.activatedHandler);
		contextService.deactivateContext(this.activatedContext);
		this.activatedContext = null;
		this.activatedHandler = null;
	}

	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getSource().equals(getProcess())) {
				DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {

					public void run() {
						RestartAction action = RestartConsolePageParticipant.this.restart;
						if (action != null) {
							action.update();
						}
					}

				});
			}
		}
	}

	protected IProcess getProcess() {
		return (this.console != null ? this.console.getProcess() : null);
	}

	private class RestartHandler extends AbstractHandler {

		public Object execute(ExecutionEvent event) throws ExecutionException {
			restart.run();
			return null;
		}

	}

}
