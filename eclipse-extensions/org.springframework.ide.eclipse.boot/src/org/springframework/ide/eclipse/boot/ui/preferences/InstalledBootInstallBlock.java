/*******************************************************************************
 *  Copyright (c) 2012,2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.launching.JavaRuntime;
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
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.cli.BootInstallManager;
import org.springframework.ide.eclipse.boot.core.cli.install.IBootInstall;
import org.springframework.ide.eclipse.boot.util.SWTFactory;


/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Kris De Volder
 */
public class InstalledBootInstallBlock implements ISelectionProvider {
	
	private static final int[] defaultColumnWidth = {
		150, 300 
	};

	/**
	 * Content provider to show a list of JREs
	 */
	class JREsContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}

		public Object[] getElements(Object input) {
			return fVMs.toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * Label provider for installed JREs table.
	 */
	class VMLabelProvider extends LabelProvider implements ITableLabelProvider {

		/**
		 * @see ITableLabelProvider#getColumnImage(Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			//TODO: nice icon for a boot install?
//			if (columnIndex == 0) {
//				return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);
//			}
			return null;
		}

		/**
		 * @see ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof IBootInstall) {
				IBootInstall vm = (IBootInstall) element;
				switch (columnIndex) {
				case 0:
					return vm.getName();
				case 1:
					return vm.getUrl();
				}
			}
			return element.toString();
		}

	}

	/**
	 * This block's control
	 */
	private Composite fControl;

	/**
	 * VMs being displayed
	 */
	private final List<IBootInstall> fVMs = new ArrayList<IBootInstall>();

	/**
	 * The main list control
	 */
	private CheckboxTableViewer fVMList;

	// Action buttons
	private Button fAddButton;

	private Button fRemoveButton;

	private Button fEditButton;
	
	private Button fExtensions;

	// index of column used for sorting
	private int fSortColumn = 0;

	/**
	 * Selection listeners (checked JRE changes)
	 */
	private final ListenerList fSelectionListeners = new ListenerList();

	/**
	 * Previous selection
	 */
	private ISelection fPrevSelection = new StructuredSelection();

	private Table fTable;

	private BootInstallManager installManager;

	public InstalledBootInstallBlock(BootInstallManager installManager) {
		this.installManager = installManager;
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.add(listener);
	}

	public void createControl(Composite ancestor) {
		Font font = ancestor.getFont();
		Composite parent = SWTFactory.createComposite(ancestor, font, 2, 1, GridData.FILL_BOTH);
		fControl = parent;

		SWTFactory.createLabel(parent, "Boot Installations:", 2);

		fTable = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 250;
		gd.widthHint = 350;
		fTable.setLayoutData(gd);
		fTable.setFont(font);
		fTable.setHeaderVisible(true);
		fTable.setLinesVisible(true);

		TableColumn column = new TableColumn(fTable, SWT.NULL);
		column.setText("Name");
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sortByName();
			}
		});

		column = new TableColumn(fTable, SWT.NULL);
		column.setText("Location");
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sortByLocation();
			}
		});

		fVMList = new CheckboxTableViewer(fTable);
		fVMList.setLabelProvider(new VMLabelProvider());
		fVMList.setContentProvider(new JREsContentProvider());
		// by default, sort by name
		sortByName();

		fVMList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent evt) {
				enableButtons();
			}
		});

		fVMList.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					setCheckedJRE((IBootInstall) event.getElement());
				}
				else {
					setCheckedJRE(null);
				}
			}
		});

		fVMList.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				if (!fVMList.getSelection().isEmpty()) {
					editVM();
				}
			}
		});
		fTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					if (fRemoveButton.isEnabled()) {
						removeVMs();
					}
				}
			}
		});

		Composite buttons = SWTFactory.createComposite(parent, font, 1, 1, GridData.VERTICAL_ALIGN_BEGINNING, 0, 0);

		fAddButton = SWTFactory.createPushButton(buttons, "Add...", null);
		fAddButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				addVM();
			}
		});

		fEditButton = SWTFactory.createPushButton(buttons, "Edit...", null);
		fEditButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				editVM();
			}
		});
		
		fExtensions = SWTFactory.createPushButton(buttons, "Extensions...", null);
		fExtensions.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				showExtensions();
			}
		});

		fRemoveButton = SWTFactory.createPushButton(buttons, "Remove...", null);
		fRemoveButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				removeVMs();
			}
		});

		SWTFactory.createVerticalSpacer(parent, 1);

		fillWithWorkspaceJREs();
		enableButtons();
		fAddButton.setEnabled(JavaRuntime.getVMInstallTypes().length > 0);
	}

	private void showExtensions() {
		IStructuredSelection selection = (IStructuredSelection) fVMList.getSelection();
		new ExtensionsDialog(getShell(), (IBootInstall) selection.getFirstElement()).open();
	}

	/**
	 * Compares the given name against current names and adds the appropriate
	 * numerical suffix to ensure that it is unique.
	 * @param name the name with which to ensure uniqueness
	 * @return the unique version of the given name
	 * @since 3.2
	 */
	public String generateName(String name, IBootInstall install) {
		if (!isDuplicateName(name, install)) {
			return name;
		}

		if (name.matches(".*\\(\\d*\\)")) { //$NON-NLS-1$
			int start = name.lastIndexOf('(');
			int end = name.lastIndexOf(')');
			String stringInt = name.substring(start + 1, end);
			int numericValue = Integer.parseInt(stringInt);
			String newName = name.substring(0, start + 1) + (numericValue + 1) + ")"; //$NON-NLS-1$
			return generateName(newName, install);
		}
		else {
			return generateName(name + " (1)", install); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the checked JRE or <code>null</code> if none.
	 * 
	 * @return the checked JRE or <code>null</code> if none
	 */
	public IBootInstall getCheckedJRE() {
		Object[] objects = fVMList.getCheckedElements();
		if (objects.length == 0) {
			return null;
		}
		return (IBootInstall) objects[0];
	}

	/**
	 * Returns this block's control
	 * 
	 * @return control
	 */
	public Control getControl() {
		return fControl;
	}

	/**
	 * Returns the JREs currently being displayed in this block
	 * 
	 * @return JREs currently being displayed in this block
	 */
	public IBootInstall[] getJREs() {
		return fVMs.toArray(new IBootInstall[fVMs.size()]);
	}

	public ISelection getSelection() {
		return new StructuredSelection(fVMList.getCheckedElements());
	}

	public boolean isDuplicateName(String name, IBootInstall install) {
		if (install != null) {
			if (install.getName() != null && install.getName().equals(name)) {
				return false;
			}
		}
		for (int i = 0; i < fVMs.size(); i++) {
			IBootInstall vm = fVMs.get(i);
			if (vm.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes the given VMs from the table.
	 * 
	 * @param vms
	 */
	public void removeJREs(IBootInstall[] vms) {
		IStructuredSelection prev = (IStructuredSelection) getSelection();
		for (IBootInstall vm : vms) {
			fVMs.remove(vm);
		}
		fVMList.refresh();
		IStructuredSelection curr = (IStructuredSelection) getSelection();
		if (!curr.equals(prev)) {
			IBootInstall[] installs = getJREs();
			if (curr.size() == 0 && installs.length == 1) {
				// pick a default VM automatically
				setSelection(new StructuredSelection(installs[0]));
			}
			else {
				fireSelectionChanged();
			}
		}
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.remove(listener);
	}

	/**
	 * Restore table settings from the given dialog store using the given key.
	 * 
	 * @param settings dialog settings store
	 * @param qualifier key to restore settings from
	 */
	public void restoreColumnSettings(IDialogSettings settings, String qualifier) {
		fVMList.getTable().layout(true);
		restoreColumnWidths(settings, qualifier);
		try {
			fSortColumn = settings.getInt(qualifier + ".sortColumn"); //$NON-NLS-1$
		}
		catch (NumberFormatException e) {
			fSortColumn = 1;
		}
		switch (fSortColumn) {
		case 1:
			sortByName();
			break;
		case 2:
			sortByLocation();
			break;
		}
	}

	/**
	 * Persist table settings into the give dialog store, prefixed with the
	 * given key.
	 * 
	 * @param settings dialog store
	 * @param qualifier key qualifier
	 */
	public void saveColumnSettings(IDialogSettings settings, String qualifier) {
		int columnCount = fTable.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			settings.put(qualifier + ".columnWidth" + i, fTable.getColumn(i).getWidth()); //$NON-NLS-1$
		}
		settings.put(qualifier + ".sortColumn", fSortColumn); //$NON-NLS-1$
	}

	/**
	 * Sets the checked JRE, possible <code>null</code>
	 * 
	 * @param vm JRE or <code>null</code>
	 */
	public void setCheckedJRE(IBootInstall vm) {
		if (vm == null) {
			setSelection(new StructuredSelection());
		}
		else {
			setSelection(new StructuredSelection(vm));
		}
	}

	public void setSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			if (!selection.equals(fPrevSelection)) {
				fPrevSelection = selection;
				Object jre = ((IStructuredSelection) selection).getFirstElement();
				if (jre == null) {
					fVMList.setCheckedElements(new Object[0]);
				}
				else {
					fVMList.setCheckedElements(new Object[] { jre });
					fVMList.reveal(jre);
				}
				fireSelectionChanged();
			}
		}
	}

	public void vmAdded(IBootInstall vm) {
		fVMs.add(vm);
		fVMList.refresh();
	}

	/**
	 * Bring up a wizard that lets the user create a new VM definition.
	 */
	private void addVM() {
		try {
			BootInstallDialog wizard = new BootInstallDialog(getShell(), installManager.newInstall("", null), this, installManager);
			if (wizard.open() == Window.OK) {
				IBootInstall result = wizard.getResult();
				if (result != null) {
					fVMs.add(result);
					fVMList.refresh();
					fVMList.setSelection(new StructuredSelection(result));
					setSelection(new StructuredSelection(result));
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
	}

	/**
	 * Performs the edit VM action when the Edit... button is pressed
	 */
	private void editVM() {
		IStructuredSelection selection = (IStructuredSelection) fVMList.getSelection();
		IBootInstall vm = (IBootInstall) selection.getFirstElement();
		if (vm == null) {
			return;
		}
		BootInstallDialog wizard = new BootInstallDialog(getShell(), vm, this, installManager);
		if (wizard.open() == Window.OK) {
			IBootInstall result = wizard.getResult();
			if (result != null) {
				// replace with the edited VM
				int index = fVMs.indexOf(vm);
				fVMs.remove(index);
				fVMs.add(index, result);
				fVMList.refresh();
				fVMList.setSelection(new StructuredSelection(result));
			}
		}
	}

	/**
	 * Enables the buttons based on selected items counts in the viewer
	 */
	private void enableButtons() {
		IStructuredSelection selection = (IStructuredSelection) fVMList.getSelection();
		int selectionCount = selection.size();
		fEditButton.setEnabled(selectionCount == 1);
		fExtensions.setEnabled(selectionCount == 1);
		if (selectionCount > 0 && selectionCount < fVMList.getTable().getItemCount()) {
			fRemoveButton.setEnabled(true);
		}
		else {
			fRemoveButton.setEnabled(false);
		}
	}

	/**
	 * Fire current selection
	 */
	private void fireSelectionChanged() {
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		Object[] listeners = fSelectionListeners.getListeners();
		for (Object listener2 : listeners) {
			ISelectionChangedListener listener = (ISelectionChangedListener) listener2;
			listener.selectionChanged(event);
		}
	}

	/**
	 * Performs the remove VM(s) action when the Remove... button is pressed
	 */
	@SuppressWarnings("unchecked")
	private void removeVMs() {
		IStructuredSelection selection = (IStructuredSelection) fVMList.getSelection();
		IBootInstall[] vms = new IBootInstall[selection.size()];
		Iterator<IBootInstall> iter = selection.iterator();
		int i = 0;
		while (iter.hasNext()) {
			vms[i] = iter.next();
			i++;
		}
		removeJREs(vms);
	}

	/**
	 * Restores the column widths from dialog settings
	 * @param settings
	 * @param qualifier
	 */
	private void restoreColumnWidths(IDialogSettings settings, String qualifier) {
		int columnCount = fTable.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			int width = -1;
			try {
				width = settings.getInt(qualifier + ".columnWidth" + i); //$NON-NLS-1$
			}
			catch (NumberFormatException e) {
			}

			if (width <= 0) {
				fTable.getColumn(i).setWidth(defaultColumnWidth[i]);
			}
			else {
				fTable.getColumn(i).setWidth(width);
			}
		}
	}

	/**
	 * Sorts by VM location.
	 */
	private void sortByLocation() {
		fVMList.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if ((e1 instanceof IBootInstall) && (e2 instanceof IBootInstall)) {
					IBootInstall left = (IBootInstall) e1;
					IBootInstall right = (IBootInstall) e2;
					return left.getUrl().compareToIgnoreCase(right.getUrl());
				}
				return super.compare(viewer, e1, e2);
			}

			@Override
			public boolean isSorterProperty(Object element, String property) {
				return true;
			}
		});
		fSortColumn = 2;
	}

	/**
	 * Sorts by VM name.
	 */
	private void sortByName() {
		fVMList.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if ((e1 instanceof IBootInstall) && (e2 instanceof IBootInstall)) {
					IBootInstall left = (IBootInstall) e1;
					IBootInstall right = (IBootInstall) e2;
					return left.getName().compareToIgnoreCase(right.getName());
				}
				return super.compare(viewer, e1, e2);
			}

			@Override
			public boolean isSorterProperty(Object element, String property) {
				return true;
			}
		});
		fSortColumn = 1;
	}

	/**
	 * Populates the JRE table with existing JREs defined in the workspace.
	 */
	protected void fillWithWorkspaceJREs() {
		Collection<IBootInstall> installs = installManager.getInstalls();
		IBootInstall deflt = null;
		try {
			deflt = installManager.getDefaultInstall();
		} catch (Exception e) {
			BootActivator.log(e);
		}
		setJREs(installs.toArray(new IBootInstall[installs.size()]), deflt);
	}

	protected Shell getShell() {
		return getControl().getShell();
	}

	/**
	 * Sets the JREs to be displayed in this block
	 * 
	 * @param vms JREs to be displayed
	 */
	protected void setJREs(IBootInstall[] vms, IBootInstall dflt) {
		fVMs.clear();
		for (IBootInstall vm : vms) {
			fVMs.add(vm);
		}
		fVMList.setInput(fVMs);
		fVMList.refresh();
		
		for (IBootInstall install : vms) {
			if (install.equals(dflt)) {
				setCheckedJRE(install);
			}
		}
	}

}
