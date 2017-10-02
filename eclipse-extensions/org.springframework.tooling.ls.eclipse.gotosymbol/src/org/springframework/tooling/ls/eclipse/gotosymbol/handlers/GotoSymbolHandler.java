/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Michał Niewrzał (Rogue Wave Software Inc.) - initial implementation
 *  Kris De Volder (Pivotal) - copied and adapted from org.eclipse.lsp4e.operations.symbols.LSPSymbolInFileDialog 
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.LanguageServiceAccessor.LSPDocumentInfo;
import org.eclipse.lsp4j.Location;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.GotoSymbolDialog;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.GotoSymbolDialogModel;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.InFileSymbolsProvider;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.InWorkspaceSymbolsProvider;

@SuppressWarnings("restriction")
public class GotoSymbolHandler extends AbstractHandler {
	
	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}
	private static GotoSymbolDialogModel currentDialog = null;

	public GotoSymbolHandler() {
		debug("Creating GotoSymbolHandler");
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		debug(">>>GotoSymbolHandler.execute");
		if (currentDialog!=null) {
			debug("GotoSymbolDialog already open: send 'toggle' signal");
			currentDialog.toggleSymbolsProvider();
		} else {
			IEditorPart part = HandlerUtil.getActiveEditor(event);
			if (part instanceof ITextEditor) {
				final Shell shell = HandlerUtil.getActiveShell(event);
				final ITextEditor textEditor = (ITextEditor) part;
				
				GotoSymbolDialogModel model = new GotoSymbolDialogModel(getKeybindings(event), InFileSymbolsProvider.createFor(textEditor), InWorkspaceSymbolsProvider.createFor(event))
				.setOkHandler(symbolInformation -> {
					if (symbolInformation!=null) {
						Location location = symbolInformation.getLocation();
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						LSPEclipseUtils.openInEditor(location, page);
					}
					return true;
				});
				GotoSymbolDialog dialog = new GotoSymbolDialog(shell, textEditor, model, /*alignRight*/ false);
				currentDialog = model;
				dialog.open();
				debug("GotoSymbolDialog opened");
				dialog.getShell().addDisposeListener(de -> {
					debug("GotoSymbolDialog closed!");
					currentDialog = null;
				});
			}
			debug("<<<GotoSymbolHandler.execute");
		}
		return null;
	}

	private String getKeybindings(ExecutionEvent event) {
		Command invokingCommand = event.getCommand();
		IBindingService service = PlatformUI.getWorkbench().getService(IBindingService.class);
		TriggerSequence[] bindings = service.getActiveBindingsFor(invokingCommand.getId());
		if (bindings!=null && bindings.length>0) {
			return bindings[0].toString();
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		boolean r = _isEnabled();
		return r;
	}
	public boolean _isEnabled() {
		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		if (part instanceof ITextEditor) {
			debug("activePart instanceof ITextEditor");
			List<LSPDocumentInfo> infos = LanguageServiceAccessor.getLSPDocumentInfosFor(
					LSPEclipseUtils.getDocument((ITextEditor) part),
					(capabilities) -> Boolean.TRUE.equals(capabilities.getDocumentSymbolProvider()));
			return !infos.isEmpty();
		}
		debug("activePart not ITextEditor: "+part);
		return false;
	}
}
