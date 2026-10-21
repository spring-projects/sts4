/*******************************************************************************
 * Copyright (c) 2016, 2026 Rogue Wave Software Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Michał Niewrzał (Rogue Wave Software Inc.) - initial implementation
 *  Kris De Volder (Pivotal) - copied and adapted from org.eclipse.lsp4e.operations.symbols.LSPSymbolInFileDialog 
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.GotoSymbolDialog;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.GotoSymbolDialogModel;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.InFileSymbolsProvider;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.InProjectSymbolsProvider;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.InWorkspaceSymbolsProvider;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.SelectionTracker.DocumentData;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.SymbolsProvider;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.SymbolsProviderScopePreference;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

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
		if (currentDialog != null) {
			debug("GotoSymbolDialog already open: send 'toggle' signal");
			currentDialog.toggleSymbolsProvider();
		} else {
			IWorkbenchPart part = getActiveWorkbenchPart(HandlerUtil.getActivePart(event));
			ITextEditor textEditor = part instanceof ITextEditor ? (ITextEditor) part : null;

			// Provider order is: workspace, project, file. The scope is selected by index.
			List<SymbolsProvider> providers = new ArrayList<>();
			if (InWorkspaceSymbolsProvider.hasActiveServer()) {
				providers.add(InWorkspaceSymbolsProvider.createForWorkspace());
			}
			int initialIndex;
			DocumentData documentData = null;
			if (textEditor != null) {
				// The project scope needs a project, the file scope needs a language server for the document
				SymbolsProvider projectProvider = InProjectSymbolsProvider.createFor(event);
				if (projectProvider != null) {
					providers.add(projectProvider);
				}
				if (hasDocumentSymbolServer(textEditor)) {
					providers.add(InFileSymbolsProvider.createFor(textEditor));
				}
				// The remembered scope may not be available for this editor
				initialIndex = Math.min(SymbolsProviderScopePreference.INSTANCE.getIndex(), providers.size() - 1);
			} else {
				// No text editor is active (e.g. Project / Package Explorer has the focus). The initial scope is the one
				// closest to the selection: file for a file, project for a project, source folder, package etc.
				// and workspace if there is nothing selected.
				IResource resource = resourceFor(HandlerUtil.getCurrentSelection(event));
				IProject selectedProject = resource == null ? null : resource.getProject();
				// Project and file scopes only make sense if a language server has started for the selected project
				if (InWorkspaceSymbolsProvider.hasActiveServer(selectedProject)) {
					providers.add(InProjectSymbolsProvider.createFor(LiveExpression.constant(selectedProject)));
					if (resource instanceof IFile file) {
						documentData = new DocumentData(file);
						providers.add(InFileSymbolsProvider.createFor(LiveExpression.constant(documentData)));
					}
				}
				initialIndex = providers.size() - 1;
			}
			if (providers.isEmpty()) {
				return null;
			}

			GotoSymbolDialogModel model = new GotoSymbolDialogModel(getKeybindings(event), initialIndex, providers.toArray(new SymbolsProvider[0]))
					.setOkHandler(GotoSymbolDialogModel.OPEN_IN_EDITOR_OK_HANDLER);
			// Without an editor no part is passed, so the dialog is sized and positioned relative to the shell, not the active view
			GotoSymbolDialog dialog = new GotoSymbolDialog(HandlerUtil.getActiveShell(event), textEditor, model, /*alignRight*/ false);
			currentDialog = model;
			dialog.open();
			debug("GotoSymbolDialog opened");
			final DocumentData dialogDocumentData = documentData;
			dialog.getShell().addDisposeListener(de -> {
				debug("GotoSymbolDialog closed!");
				if (textEditor != null) {
					// Only remember the scope the user picked in an editor, not one derived from an explorer selection
					SymbolsProviderScopePreference.INSTANCE.setIndex(model.getCurrentSymbolsProviderIndex());
				}
				if (dialogDocumentData != null) {
					dialogDocumentData.dispose();
				}
				currentDialog = null;
			});
			debug("<<<GotoSymbolHandler.execute");
		}
		
		return null;
	}

	private static boolean hasDocumentSymbolServer(ITextEditor textEditor) {
		IDocument document = LSPEclipseUtils.getDocument(textEditor);
		return document != null && LanguageServers.forDocument(document)
				.withFilter(capabilities -> LSPEclipseUtils.hasCapability(capabilities.getDocumentSymbolProvider()))
				.anyMatching();
	}

	private static IResource resourceFor(ISelection selection) {
		if (selection instanceof IStructuredSelection ss && !ss.isEmpty()) {
			Object element = ss.getFirstElement();
			if (element instanceof IResource r) {
				return r;
			} else if (element instanceof IAdaptable a) {
				return a.getAdapter(IResource.class);
			}
		}
		return null;
	}

	private String getKeybindings(ExecutionEvent event) {
		Command invokingCommand = event.getCommand();
		IBindingService service = PlatformUI.getWorkbench().getService(IBindingService.class);
		TriggerSequence[] bindings = service.getActiveBindingsFor(invokingCommand.getId());
		if (bindings != null && bindings.length > 0) {
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
		return InWorkspaceSymbolsProvider.hasActiveServer();
	}
	
	private static IWorkbenchPart getActiveWorkbenchPart(IWorkbenchPart part) {
		if (part instanceof MultiPageEditorPart) {
			MultiPageEditorPart multipageEditor = (MultiPageEditorPart) part;
			Object page = multipageEditor.getSelectedPage();
			if (page instanceof IWorkbenchPart) {
				part = (IWorkbenchPart) page;
			}			
		}
		return part;
	}
}
