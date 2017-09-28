/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Michał Niewrzał (Rogue Wave Software Inc.) - initial implementation
 *  Kris De Volder (Pivotal Inc) - Copied and adapted from 
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.lsp4e.outline.SymbolsLabelProvider;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.tooling.ls.eclipse.gotosymbol.util.SwtConnect;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

@SuppressWarnings("restriction")
public class GotoSymbolDialog extends PopupDialog {

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	private static class SymbolsContentProvider implements ITreeContentProvider {
		
		@Override
		public Object[] getChildren(Object parentElement) {
			return null;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return false;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof GotoSymbolDialogModel) {
				GotoSymbolDialogModel model = (GotoSymbolDialogModel) inputElement;
				return model.getSymbols().getValues().toArray();
			}
			return null;
		}
	}

	private GotoSymbolDialogModel model;
	private List<Disposable> disposables = new ArrayList<>();
	private ITextEditor fTextEditor;

	public GotoSymbolDialog(Shell parentShell, ITextEditor textEditor, GotoSymbolDialogModel model) {
		super(parentShell, PopupDialog.INFOPOPUP_SHELLSTYLE, true, true, true, false, false, null, null);
		this.fTextEditor = textEditor;
		this.model = model;
		create();
	}

	private void installKeyListeners(Text pattern, TreeViewer list) {
		pattern.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					if (list.getTree().getItemCount() > 0) {
						list.getTree().setFocus();
						TreeItem[] items = list.getTree().getItems();
						if (items!=null && items.length>0) {
							list.getTree().setSelection(items[0]);
							//programatic selection may not fire selection events so...
							list.getTree().notifyListeners(SWT.Selection,
									new Event());
						}
					}
				}
			}
		});

		list.getTree().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {

				if (e.keyCode == SWT.ARROW_UP && (e.stateMask & SWT.SHIFT) == 0
						&& (e.stateMask & SWT.CTRL) == 0) {
					StructuredSelection selection = (StructuredSelection) list
							.getSelection();

					if (selection.size() == 1) {
						Object element = selection.getFirstElement();
						if (element.equals(getFirstElement(list))) {
							pattern.setFocus();
							list.setSelection(new StructuredSelection());
							list.getTree().notifyListeners(SWT.Selection,
									new Event());
						}

					}
				}

//				if (e.keyCode == SWT.ARROW_DOWN
//						&& (e.stateMask & SWT.SHIFT) != 0
//						&& (e.stateMask & SWT.CTRL) != 0) {
//
//					list.getTree().notifyListeners(SWT.Selection, new Event());
//				}

			}
		});
	}
	
	private Object getFirstElement(TreeViewer list) {
		TreeItem[] items = list.getTree().getItems();
		if (items!=null && items.length>0) {
			TreeItem item = items[0];
			return item.getData();
		}
		return null;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = new Composite(parent, SWT.NONE);
		dialogArea.addDisposeListener(de -> {
			for (Disposable d : disposables) {
				d.dispose();
			}
		});
		if (parent.getLayout() instanceof GridLayout) {
			dialogArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		dialogArea.setLayout(layout);
		
		Text pattern = new Text(dialogArea, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
//		pattern.getAccessible().addAccessibleListener(new AccessibleAdapter() {
//			public void getName(AccessibleEvent e) {
//				e.result = LegacyActionTools.removeMnemonics(headerLabel)
//						.getText());
//			}
//		});
		pattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		SwtConnect.connect(pattern, model.getSearchBox());

		TreeViewer viewer = new TreeViewer(dialogArea);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());
		viewer.setContentProvider(new SymbolsContentProvider());
		viewer.setLabelProvider(new SymbolsLabelProvider());
		viewer.setUseHashlookup(true);
		disposables.add(model.getSymbols().onChange((e, v) -> {
			viewer.refresh();
		}));
		if (DEBUG) {
			viewer.addSelectionChangedListener(event -> {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.isEmpty()) {
					debug("selected NONE");
				} else {
					SymbolInformation symbolInformation = (SymbolInformation) selection.getFirstElement();
					debug("selected = "+symbolInformation.getName());
				}
			});
		}
//TODO: somehow show selection in local file, (but not in other file ?)
//		viewer.addSelectionChangedListener(event -> {
//			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
//			if (selection.isEmpty()) {
//				return;
//			}
//			SymbolInformation symbolInformation = (SymbolInformation) selection.getFirstElement();
//			Location location = symbolInformation.getLocation();
//
//			IResource targetResource = LSPEclipseUtils.findResourceFor(location.getUri());
//			if (targetResource == null) {
//				return;
//			}
//			IDocument targetDocument = FileBuffers.getTextFileBufferManager()
//			        .getTextFileBuffer(targetResource.getFullPath(), LocationKind.IFILE).getDocument();
//			if (targetDocument != null) {
//				try {
//					int offset = LSPEclipseUtils.toOffset(location.getRange().getStart(), targetDocument);
//					int endOffset = LSPEclipseUtils.toOffset(location.getRange().getEnd(), targetDocument);
//					fTextEditor.selectAndReveal(offset, endOffset - offset);
//				} catch (BadLocationException e) {
//					LanguageServerPlugin.logError(e);
//				}
//			}
//		});
		installKeyListeners(pattern, viewer);
		
		Label statusLabel = new Label(dialogArea, SWT.NONE);
		statusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		SwtConnect.connect(statusLabel, model.getStatus());
		
		viewer.setInput(model);
		return dialogArea;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);

		shell.setSize(280, 300);
		Control control = fTextEditor.getAdapter(Control.class);
		if (control != null) {
			shell.setLocation(
			        control.toDisplay(control.getBounds().width - shell.getSize().x, control.getLocation().y));
		}
	}

}
