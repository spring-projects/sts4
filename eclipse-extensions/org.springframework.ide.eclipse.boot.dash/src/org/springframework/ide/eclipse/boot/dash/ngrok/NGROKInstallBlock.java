/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.ngrok;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.util.SWTFactory;


/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Kris De Volder
 */
public class NGROKInstallBlock implements ISelectionProvider {

	private static final int defaultColumnWidth = 350;

	class NGROKContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}

		public Object[] getElements(Object input) {
			return ngroks.toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class NGROKLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			return element.toString();
		}

	}

	private Composite control;
	private final List<String> ngroks = new ArrayList<String>();
	private CheckboxTableViewer ngrokList;
	private Button addButton;
	private Button removeButton;
	private Button editButton;

	private final ListenerList selectionListeners = new ListenerList();
	private ISelection prevSelection = new StructuredSelection();
	private Table table;

	private NGROKInstallManager installManager;

	public NGROKInstallBlock(NGROKInstallManager installManager) {
		this.installManager = installManager;
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.add(listener);
	}

	public void createControl(Composite ancestor) {
		Font font = ancestor.getFont();
		Composite parent = SWTFactory.createComposite(ancestor, font, 2, 1, GridData.FILL_BOTH);
		control = parent;

		SWTFactory.createLabel(parent, "ngrok Installations:", 2);

		table = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 250;
		gd.widthHint = 350;
		table.setLayoutData(gd);
		table.setFont(font);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn column = new TableColumn(table, SWT.NULL);
		column.setText("Location");
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sortByLocation();
			}
		});

		ngrokList = new CheckboxTableViewer(table);
		ngrokList.setLabelProvider(new NGROKLabelProvider());
		ngrokList.setContentProvider(new NGROKContentProvider());
		// by default, sort by name
		sortByLocation();

		ngrokList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent evt) {
				enableButtons();
			}
		});

		ngrokList.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					setCheckedNGROK((String) event.getElement());
				}
				else {
					setCheckedNGROK(null);
				}
			}
		});

		ngrokList.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				if (!ngrokList.getSelection().isEmpty()) {
					editNGROK();
				}
			}
		});
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					if (removeButton.isEnabled()) {
						removeNGROKs();
					}
				}
			}
		});

		Composite buttons = SWTFactory.createComposite(parent, font, 1, 1, GridData.VERTICAL_ALIGN_BEGINNING, 0, 0);

		addButton = SWTFactory.createPushButton(buttons, "Add...", null);
		addButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				addNGROK();
			}
		});

		editButton = SWTFactory.createPushButton(buttons, "Edit...", null);
		editButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				editNGROK();
			}
		});

		removeButton = SWTFactory.createPushButton(buttons, "Remove...", null);
		removeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				removeNGROKs();
			}
		});

		SWTFactory.createVerticalSpacer(parent, 1);

		fillWithWorkspaceNGROKs();
		enableButtons();
		addButton.setEnabled(true);
	}

	public String getCheckedNGROK() {
		Object[] objects = ngrokList.getCheckedElements();
		if (objects.length == 0) {
			return null;
		}
		return (String) objects[0];
	}

	public Control getControl() {
		return control;
	}

	public String[] getNGROKs() {
		return ngroks.toArray(new String[ngroks.size()]);
	}

	public ISelection getSelection() {
		return new StructuredSelection(ngrokList.getCheckedElements());
	}

	public boolean isDuplicateName(String name) {
		for (int i = 0; i < ngroks.size(); i++) {
			String ngrok = ngroks.get(i);
			if (ngrok.equals(name)) {
				return true;
			}
		}
		return false;
	}

	public void removeNGROKs(String[] ngroksToBeRemoved) {
		IStructuredSelection prev = (IStructuredSelection) getSelection();
		for (String ngrok : ngroksToBeRemoved) {
			ngroks.remove(ngrok);
		}
		ngrokList.refresh();

		IStructuredSelection curr = (IStructuredSelection) getSelection();
		if (!curr.equals(prev)) {
			String[] installs = getNGROKs();
			if (curr.size() == 0 && installs.length == 1) {
				setSelection(new StructuredSelection(installs[0]));
			}
			else {
				fireSelectionChanged();
			}
		}
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.remove(listener);
	}

	public void restoreColumnSettings(IDialogSettings settings, String qualifier) {
		ngrokList.getTable().layout(true);
		restoreColumnWidths(settings, qualifier);
	}

	public void saveColumnSettings(IDialogSettings settings, String qualifier) {
		int columnCount = table.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			settings.put(qualifier + ".columnWidth" + i, table.getColumn(i).getWidth()); //$NON-NLS-1$
		}
	}

	public void setCheckedNGROK(String vm) {
		if (vm == null) {
			setSelection(new StructuredSelection());
		}
		else {
			setSelection(new StructuredSelection(vm));
		}
	}

	public void setSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			if (!selection.equals(prevSelection)) {
				prevSelection = selection;
				Object ngrok = ((IStructuredSelection) selection).getFirstElement();
				if (ngrok == null) {
					ngrokList.setCheckedElements(new Object[0]);
				}
				else {
					ngrokList.setCheckedElements(new Object[] { ngrok });
					ngrokList.reveal(ngrok);
				}
				fireSelectionChanged();
			}
		}
	}

	public void ngrokAdded(String ngrok) {
		ngroks.add(ngrok);
		ngrokList.refresh();
	}

	private void addNGROK() {
		try {
			FileDialog fileDialog = new FileDialog(control.getShell());
			fileDialog.setText("select ngrok executable");

			String result = fileDialog.open();
			if (result != null) {
				ngroks.add(result);
				ngrokList.refresh();
				ngrokList.setSelection(new StructuredSelection(result));
				setSelection(new StructuredSelection(result));
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
	}

	private void editNGROK() {
		IStructuredSelection selection = (IStructuredSelection) ngrokList.getSelection();
		String ngrok = (String) selection.getFirstElement();
		if (ngrok == null) {
			return;
		}

		FileDialog fileDialog = new FileDialog(control.getShell());
		fileDialog.setText("select ngrok executable");
		fileDialog.setFileName(ngrok);

		String result = fileDialog.open();
		if (result != null) {
			// replace with the edited VM
			int index = ngroks.indexOf(ngrok);
			ngroks.remove(index);
			ngroks.add(index, result);
			ngrokList.refresh();
			ngrokList.setSelection(new StructuredSelection(result));
		}
	}

	private void enableButtons() {
		IStructuredSelection selection = (IStructuredSelection) ngrokList.getSelection();

		int selectionCount = selection.size();
		editButton.setEnabled(selectionCount == 1);
		if (selectionCount > 0 && selectionCount < ngrokList.getTable().getItemCount()) {
			removeButton.setEnabled(true);
		}
		else {
			removeButton.setEnabled(false);
		}
	}

	private void fireSelectionChanged() {
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		Object[] listeners = selectionListeners.getListeners();
		for (Object listener2 : listeners) {
			ISelectionChangedListener listener = (ISelectionChangedListener) listener2;
			listener.selectionChanged(event);
		}
	}

	private void removeNGROKs() {
		IStructuredSelection selection = (IStructuredSelection) ngrokList.getSelection();
		String[] ngroks = new String[selection.size()];
		Iterator<String> iter = selection.iterator();

		int i = 0;
		while (iter.hasNext()) {
			ngroks[i] = iter.next();
			i++;
		}
		removeNGROKs(ngroks);
	}

	private void restoreColumnWidths(IDialogSettings settings, String qualifier) {
		int columnCount = table.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			int width = -1;
			try {
				width = settings.getInt(qualifier + ".columnWidth" + i); //$NON-NLS-1$
			}
			catch (NumberFormatException e) {
			}

			if (width <= 0) {
				table.getColumn(i).setWidth(defaultColumnWidth);
			}
			else {
				table.getColumn(i).setWidth(width);
			}
		}
	}

	private void sortByLocation() {
		ngrokList.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if ((e1 instanceof String) && (e2 instanceof String)) {
					String left = (String) e1;
					String right = (String) e2;
					return left.compareToIgnoreCase(right);
				}
				return super.compare(viewer, e1, e2);
			}

			@Override
			public boolean isSorterProperty(Object element, String property) {
				return true;
			}
		});
	}

	protected void fillWithWorkspaceNGROKs() {
		Collection<String> installs = installManager.getInstalls();
		String deflt = null;
		try {
			deflt = installManager.getDefaultInstall();
		} catch (Exception e) {
			BootActivator.log(e);
		}
		setNGROKs(installs.toArray(new String[installs.size()]), deflt);
	}

	protected Shell getShell() {
		return getControl().getShell();
	}

	protected void setNGROKs(String[] newNgroks, String dflt) {
		ngroks.clear();
		for (String ngrok : newNgroks) {
			ngroks.add(ngrok);
		}
		ngrokList.setInput(ngroks);
		ngrokList.refresh();

		for (String ngrok : newNgroks) {
			if (ngrok.equals(dflt)) {
				setCheckedNGROK(ngrok);
			}
		}
	}

}
