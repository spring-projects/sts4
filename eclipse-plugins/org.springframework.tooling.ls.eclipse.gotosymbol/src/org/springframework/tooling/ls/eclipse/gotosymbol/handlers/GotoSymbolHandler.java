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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.LanguageServiceAccessor.LSPDocumentInfo;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.GotoSymbolDialog;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.GotoSymbolDialogModel;

import com.google.common.collect.ImmutableList;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
@SuppressWarnings("restriction")
public class GotoSymbolHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("GotoSymbolHandler.execute");
		IEditorPart part = HandlerUtil.getActiveEditor(event);
		if (part instanceof ITextEditor) {
			final ITextEditor textEditor = (ITextEditor) part;
			Collection<LSPDocumentInfo> infos = LanguageServiceAccessor.getLSPDocumentInfosFor(
					LSPEclipseUtils.getDocument(textEditor),
					capabilities -> Boolean.TRUE.equals(capabilities.getDocumentSymbolProvider()));
			if (infos.isEmpty()) {
				return null;
			}
			// TODO maybe consider better strategy such as iterating on all LS until we have a good result
			LSPDocumentInfo info = infos.iterator().next();
			final Shell shell = HandlerUtil.getActiveShell(event);
			DocumentSymbolParams params = new DocumentSymbolParams(
					new TextDocumentIdentifier(info.getFileUri().toString()));
			CompletableFuture<List<? extends SymbolInformation>> symbols = info.getLanguageClient()
					.getTextDocumentService().documentSymbol(params);
			
			symbols.thenAccept((List<? extends SymbolInformation> t) -> {
				shell.getDisplay().asyncExec(() -> {
					List<SymbolInformation> allSymbols = ImmutableList.copyOf(t);
					GotoSymbolDialogModel model = new GotoSymbolDialogModel(query -> allSymbols);
					GotoSymbolDialog dialog = new GotoSymbolDialog(shell, textEditor, model);
					dialog.open();
				});
			});
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		boolean r = _isEnabled();
		System.out.println("isEnabled = "+r);
		return r;
	}
	public boolean _isEnabled() {

		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		if (part instanceof ITextEditor) {
			List<LSPDocumentInfo> infos = LanguageServiceAccessor.getLSPDocumentInfosFor(
					LSPEclipseUtils.getDocument((ITextEditor) part),
					(capabilities) -> Boolean.TRUE.equals(capabilities.getDocumentSymbolProvider()));
			return !infos.isEmpty();
		}
		return false;
	}
}
