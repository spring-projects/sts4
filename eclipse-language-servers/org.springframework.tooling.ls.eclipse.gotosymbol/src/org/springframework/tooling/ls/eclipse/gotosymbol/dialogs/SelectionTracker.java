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
package org.springframework.tooling.ls.eclipse.gotosymbol.dialogs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.springframework.ide.eclipse.boot.dash.model.AbstractDisposable;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class SelectionTracker extends AbstractDisposable {
	
	private static Map<IWorkbenchWindow, SelectionTracker> INSTANCES = new HashMap<>();
	
	public static synchronized SelectionTracker getInstance(IWorkbenchWindow wbw) {
		return INSTANCES.computeIfAbsent(wbw, _wbw -> {
			return new SelectionTracker(wbw);
		});
	}
	
	private static synchronized void disposeInstance(IWorkbenchWindow wbw) {
		SelectionTracker removed = INSTANCES.remove(wbw);
		if (removed!=null) {
			removed.dispose();
		}
	}
	
	private final LiveVariable<IResource> currentResource = new LiveVariable<>();
	public final LiveExpression<IProject> currentProject = currentResource.apply(r -> r==null ? null : r.getProject()); 
	
	private SelectionTracker(IWorkbenchWindow wbw) {
		ISelectionService selectionService = wbw.getSelectionService();
		ISelectionListener selectionListener = new ISelectionListener() {
			
			@Override
			public void selectionChanged(IWorkbenchPart arg0, ISelection selection) {
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection) selection;
					Object element = ss.getFirstElement();
					IResource rsrc = getResource(element);
					if (rsrc!=null) {
						currentResource.setValue(rsrc);
					}
				} else if (selection instanceof ITextSelection) {
					//Let's assume the selection is in the active editor
					try {
						IEditorPart editor = wbw.getActivePage().getActiveEditor();
						if (editor!=null) {
							IEditorInput input = editor.getEditorInput();
							currentResource.setValue(input.getAdapter(IResource.class));
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
		selectionService.addSelectionListener(selectionListener);
		onDispose(de -> {
			selectionService.removeSelectionListener(selectionListener);
		});
		wbw.getShell().addDisposeListener(de -> {
			disposeInstance(wbw);
		});
		
		//Code below tries to determine the 'initial' selection. 
		//Unfortunately, doesn't work. Mostly just getting null here, seems to be no way to get
		// 'initial' selection reliably.
		ISelection initialSelection = selectionService.getSelection();
		if (initialSelection!=null) {
			selectionListener.selectionChanged(null, initialSelection);
		}
	}
	
	public LiveExpression<IResource> currentResource() {
		return currentResource;
	}
}
