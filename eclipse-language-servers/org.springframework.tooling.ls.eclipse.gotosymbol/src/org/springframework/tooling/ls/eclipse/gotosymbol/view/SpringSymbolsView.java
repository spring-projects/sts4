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
package org.springframework.tooling.ls.eclipse.gotosymbol.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4j.Location;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.boot.dash.views.sections.ViewPartWithSections;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.GotoSymbolDialogModel;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.GotoSymbolSection;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.InFileSymbolsProvider;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.InProjectSymbolsProvider;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.InWorkspaceSymbolsProvider;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.SimpleLabelProvider;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("restriction")
public class SpringSymbolsView extends ViewPartWithSections {

	/**
	 * Adds scroll support to the whole view. You probably want to disable this
	 * if view is broken into pieces that have their own scrollbars
	 */
	private static final boolean ENABLE_SCROLLING = false;

	private final SpringSymbolsViewModel model = new SpringSymbolsViewModel();
	{
		model.gotoSymbols.setOkHandler(GotoSymbolDialogModel.OPEN_IN_EDITOR_OK_HANDLER);
	}

	
	private ISelectionListener selectionListener = new ISelectionListener() {
		
		@Override
		public void selectionChanged(IWorkbenchPart arg0, ISelection selection) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				Object element = ss.getFirstElement();
				IResource rsrc = getResource(element);
				if (rsrc!=null) {
					model.currentResource.setValue(rsrc);
				}
			} else if (selection instanceof ITextSelection) {
				//Let's assume the selection is in the active editor
				try {
					IEditorPart editor = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
					if (editor!=null) {
						IEditorInput input = editor.getEditorInput();
						model.currentResource.setValue(input.getAdapter(IResource.class));
					}
				} catch (Exception e) {
					Log.log(e);
				}
			}
		}

		private IResource getResource(Object element) {
			if (element instanceof IResource) {
				return (IResource) element;
			} else if (element instanceof IAdaptable) {
				return ((IAdaptable) element).getAdapter(IResource.class);
			}
			return null;
		}
	};
	
	public SpringSymbolsView() {
		super(ENABLE_SCROLLING);
	}

	@SuppressWarnings("resource")
	@Override
	protected List<IPageSection> createSections() throws CoreException {
		List<IPageSection> sections = new ArrayList<>();
		sections.add(
			new ChooseOneSectionCombo<>(this, "Scope", 
				model.gotoSymbols.currentSymbolsProvider, 
				ImmutableList.copyOf(model.gotoSymbols.getSymbolsProviders())
			)
			.setLabelProvider(new SimpleLabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof InWorkspaceSymbolsProvider) {
						return "Workspace";
					} else if (element instanceof InProjectSymbolsProvider) {
						return "Project";
					} else if (element instanceof InFileSymbolsProvider) {
						return "File";
					}
					return super.getText(element);
				}
			})
		);
		sections.add(new GotoSymbolSection(this, model.gotoSymbols).enableStatusLine(false));
		ISelectionService selectitonService = getSite().getWorkbenchWindow().getSelectionService();
		selectitonService.addSelectionListener(selectionListener);
		return sections;
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		contributeToActionBars();
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Fills the pull-down menu for this view (accessible from the toolbar)
	 */
	private void fillLocalPullDown(IMenuManager manager) {
	}

	private void fillLocalToolBar(IToolBarManager manager) {
	}
	
}
