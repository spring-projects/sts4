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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.lsp4e.outline.SymbolsLabelProvider;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
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
import org.springframework.tooling.ls.eclipse.gotosymbol.GotoSymbolPlugin;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.ui.util.SwtConnect;

@SuppressWarnings("restriction")
public class GotoSymbolDialog extends PopupDialog {
	
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
	
	private static class GotoSymbolsLabelProvider extends SymbolsLabelProvider {
		@Override
		protected int getMaxSeverity(IResource resource, SymbolInformation symbolInformation)
				throws CoreException, BadLocationException {
			int maxSeverity = -1;
			for (IMarker marker : resource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO)) {
				int offset = marker.getAttribute(IMarker.CHAR_START, -1);
				if (offset != -1) {
					maxSeverity = Math.max(maxSeverity, marker.getAttribute(IMarker.SEVERITY, -1));
				}
			}
			return maxSeverity;
		}
	}

	private GotoSymbolDialogModel model;
	private List<Disposable> disposables = new ArrayList<>();
	private ITextEditor fTextEditor;
	
	/**
	 * If true, align the dialog so it looks like its attached right side of the editor. Otherwise, dialog is centered on the editor instead.
	 */
	private boolean alignRight;
	private IDialogSettings dlgSettings;

	public GotoSymbolDialog(Shell parentShell, ITextEditor textEditor, GotoSymbolDialogModel model, boolean alignRight) {
		super(parentShell, PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE, 
				/*focus*/true, /*persistSize*/true, /*persistLoc*/true, 
				/*menu*/false, /*showPersistActions*/false, null, null
		);
		//Note: because of the way shell is initialized, the options to persist size / location do not work.
		// If we want this to work, it will have to be debugged. 
		//For the time being I've simply disabled the menu that makes it appear like this should work.
		this.fTextEditor = textEditor;
		this.model = model;
		this.alignRight = alignRight;
		create();
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		if (dlgSettings==null) {
			this.dlgSettings = GotoSymbolPlugin.getInstance().getDialogSettings();
		}
		return dlgSettings;
	}
	
	/**
	 * Determine the 'target' for the dialog's action.
	 */
	private SymbolInformation getTarget(TreeViewer list) {
		ISelection sel = list.getSelection();
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) sel;
			Object selected = ss.getFirstElement();
			if (selected!=null) {
				return (SymbolInformation) selected;
			}
		}
		//No element selected, target the first element in the list instead.
		//This allows user to execute the action without explicitly selecting an element.
		return getFirstElement(list);
	}

	private void installWidgetListeners(Text pattern, TreeViewer list) {
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
				} else if (e.character == '\r') {
					performOk(list);
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
				} else if (e.character == '\r') {
					performOk(list);
				}

//				if (e.keyCode == SWT.ARROW_DOWN
//						&& (e.stateMask & SWT.SHIFT) != 0
//						&& (e.stateMask & SWT.CTRL) != 0) {
//
//					list.getTree().notifyListeners(SWT.Selection, new Event());
//				}

			}
		});
		
		list.addDoubleClickListener(e -> performOk(list));
	}
	
	private void performOk(TreeViewer list) {
		if (model.performOk(getTarget(list))) {
			close();
		}
	}
	
	private SymbolInformation getFirstElement(TreeViewer list) {
		TreeItem[] items = list.getTree().getItems();
		if (items!=null && items.length>0) {
			TreeItem item = items[0];
			return (SymbolInformation) item.getData();
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
		pattern.setMessage(model.getSearchBoxHintMessage());

		SwtConnect.connect(pattern, model.getSearchBox());

		TreeViewer viewer = new TreeViewer(dialogArea, SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());
		viewer.setContentProvider(new SymbolsContentProvider());
		viewer.setLabelProvider(new GotoSymbolsLabelProvider());
		viewer.setUseHashlookup(true);
		disposables.add(model.getSymbols().onChange(UIValueListener.from((e, v) -> {
			viewer.refresh();
		})));
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
		installWidgetListeners(pattern, viewer);
		
		Label statusLabel = new Label(dialogArea, SWT.NONE);
		statusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		SwtConnect.connect(statusLabel, model.getStatus(), Duration.ofMillis(500));
		
		viewer.setInput(model);
		return dialogArea;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		
		Control control = fTextEditor.getAdapter(Control.class);
		if (control != null) {
			//Set size relative to editor's size.
			Point size = new Point(control.getBounds().width/3, control.getBounds().height/2);
			shell.setSize(size);
			if (alignRight) {
				shell.setLocation(control.toDisplay(control.getBounds().width - shell.getSize().x, control.getLocation().y));
			} else {
				//centered on the editor
				shell.setLocation(control.toDisplay(control.getBounds().width/3, control.getBounds().height/4));
			}
		} else {
			// fallback
			shell.setSize(280, 300);
		}
	}

//	/**
//	 * Determines the graphical area covered by the given text region.
//	 *
//	 * @param region the region whose graphical extend must be computed
//	 * @return the graphical extend of the given region
//	 */
//	private Rectangle computeArea(IRegion region) {
//
//		int start= 0;
//		int end= 0;
//
//		IRegion widgetRegion= modelRange2WidgetRange(region);
//		if (widgetRegion != null) {
//			start= widgetRegion.getOffset();
//			end= widgetRegion.getOffset() + widgetRegion.getLength();
//		}
//
//		StyledText styledText= fTextEditor.getTextWidget();
//		Rectangle bounds;
//		if (end > 0 && start < end)
//			bounds= styledText.getTextBounds(start, end - 1);
//		else {
//			Point loc= styledText.getLocationAtOffset(start);
//			bounds= new Rectangle(loc.x, loc.y, 0, styledText.getLineHeight(start));
//		}
//
//		Rectangle clientArea= styledText.getClientArea();
//		Geometry.moveInside(bounds, clientArea);
//		return bounds;
//	}


}
