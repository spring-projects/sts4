/**
 * Copied from Eclipse org.eclipse.compare.structuremergeviewer.DiffTreeViewer,
 *  and modified to extend from JFace CheckboxTreeViewer. Original copyright below
 */

/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters.eclipse;

import java.util.Iterator;
import java.util.ResourceBundle;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareViewerPane;
import org.eclipse.compare.INavigatable;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.internal.patch.DiffViewerComparator;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * A tree viewer that works on objects implementing
 * the {@code IDiffContainer} and {@code IDiffElement} interfaces.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed outside
 * of this package.
 * </p>
 *
 * @see IDiffContainer
 * @see IDiffElement
 * @noextend This class is not intended to be subclassed by clients.
 */
@SuppressWarnings("restriction")
public class CheckboxDiffTreeViewer extends CheckboxTreeViewer {

	class DiffViewerContentProvider implements ITreeContentProvider {
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Empty implementation.
		}

		public boolean isDeleted(Object element) {
			return false;
		}

		@Override
		public void dispose() {
			inputChanged(CheckboxDiffTreeViewer.this, getInput(), null);
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof IDiffElement)
				return ((IDiffElement) element).getParent();
			return null;
		}

		@Override
		public final boolean hasChildren(Object element) {
			if (element instanceof IDiffContainer)
				return ((IDiffContainer) element).hasChildren();
			return false;
		}

		@Override
		public final Object[] getChildren(Object element) {
			if (element instanceof IDiffContainer)
				return ((IDiffContainer) element).getChildren();
			return new Object[0];
		}

		@Override
		public Object[] getElements(Object element) {
			return getChildren(element);
		}
	}

	/*
	 * Takes care of swapping left and right if fLeftIsLocal is true.
	 */
	class DiffViewerLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof IDiffElement)
				return ((IDiffElement) element).getName();

			return Utilities.getString(fBundle, "defaultLabel"); //$NON-NLS-1$
		}

		@Override
		@SuppressWarnings("incomplete-switch")
		public Image getImage(Object element) {
			if (element instanceof IDiffElement) {
				IDiffElement input= (IDiffElement) element;

				int kind= input.getKind();
				// Flip the direction and the change type, because all images
				// are the other way round, i.e. for comparison from left to right.
				switch (kind & Differencer.DIRECTION_MASK) {
				case Differencer.LEFT:
					kind= (kind &~ Differencer.LEFT) | Differencer.RIGHT;
					break;
				case Differencer.RIGHT:
					kind= (kind &~ Differencer.RIGHT) | Differencer.LEFT;
					break;
				case 0:
					switch (kind & Differencer.CHANGE_TYPE_MASK) {
					case Differencer.ADDITION:
						kind= (kind &~ Differencer.ADDITION) | Differencer.DELETION;
						break;
					case Differencer.DELETION:
						kind= (kind &~ Differencer.DELETION) | Differencer.ADDITION;
						break;
					}
				}

				return fCompareConfiguration.getImage(input.getImage(), kind);
			}
			return null;
		}

		/**
		 * Informs the platform, that the images have changed.
		 */
		public void fireLabelProviderChanged() {
			fireLabelProviderChanged(new LabelProviderChangedEvent(this));
		}
	}

	static class FilterSame extends ViewerFilter {
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof IDiffElement)
				return (((IDiffElement) element).getKind() & Differencer.PSEUDO_CONFLICT) == 0;
			return true;
		}
		public boolean isFilterProperty(Object element, Object property) {
			return false;
		}
	}

	private ResourceBundle fBundle;
	private CompareConfiguration fCompareConfiguration;
	private IPropertyChangeListener fPropertyChangeListener;
	private DiffViewerLabelProvider diffViewerLabelProvider;

	private Action fEmptyMenuAction;
	private Action fExpandAllAction;

	/**
	 * Creates a new viewer for the given SWT tree control with the specified configuration.
	 *
	 * @param tree the tree control
	 * @param configuration the configuration for this viewer
	 */
	public CheckboxDiffTreeViewer(Tree tree, CompareConfiguration configuration) {
		super(tree);
		initialize(configuration == null ? new CompareConfiguration() : configuration);
	}

	/**
	 * Creates a new viewer under the given SWT parent and with the specified configuration.
	 *
	 * @param parent the SWT control under which to create the viewer
	 * @param configuration the configuration for this viewer
	 */
	public CheckboxDiffTreeViewer(Composite parent, CompareConfiguration configuration) {
		super(new Tree(parent, SWT.MULTI | SWT.CHECK));
		initialize(configuration == null ? new CompareConfiguration() : configuration);
	}

	private void initialize(CompareConfiguration configuration) {
		Control tree= getControl();

		INavigatable nav= new INavigatable() {
			@Override
			public boolean selectChange(int flag) {
				if (flag == INavigatable.FIRST_CHANGE) {
					setSelection(StructuredSelection.EMPTY);
					flag = INavigatable.NEXT_CHANGE;
				} else if (flag == INavigatable.LAST_CHANGE) {
					setSelection(StructuredSelection.EMPTY);
					flag = INavigatable.PREVIOUS_CHANGE;
				}
				// Fix for https://dev.eclipse.org/bugs/show_bug.cgi?id=20106
				return internalNavigate(flag == INavigatable.NEXT_CHANGE, true);
			}
			@Override
			public Object getInput() {
				return CheckboxDiffTreeViewer.this.getInput();
			}
			@Override
			public boolean openSelectedChange() {
				return internalOpen();
			}
			@Override
			public boolean hasChange(int changeFlag) {
				return getNextItem(changeFlag == INavigatable.NEXT_CHANGE, false) != null;
			}
		};
		tree.setData(INavigatable.NAVIGATOR_PROPERTY, nav);
		tree.setData(CompareUI.COMPARE_VIEWER_TITLE, getTitle());

		Composite parent= tree.getParent();

		fBundle= ResourceBundle.getBundle("org.eclipse.compare.structuremergeviewer.DiffTreeViewerResources"); //$NON-NLS-1$

		// Register for notification with the CompareConfiguration.
		fCompareConfiguration= configuration;
		if (fCompareConfiguration != null) {
			fPropertyChangeListener = this::propertyChange;
			fCompareConfiguration.addPropertyChangeListener(fPropertyChangeListener);
		}

		setContentProvider(new DiffViewerContentProvider());
		setLabelProvider(diffViewerLabelProvider = new DiffViewerLabelProvider());

		addSelectionChangedListener(event -> updateActions());

		setComparator(new DiffViewerComparator());

		ToolBarManager tbm= CompareViewerPane.getToolBarManager(parent);
		if (tbm != null) {
			tbm.removeAll();

			tbm.add(new Separator("merge")); //$NON-NLS-1$
			tbm.add(new Separator("modes")); //$NON-NLS-1$
			tbm.add(new Separator("navigation")); //$NON-NLS-1$

			createToolItems(tbm);
			updateActions();

			tbm.update(true);
		}

		MenuManager mm= new MenuManager();
		mm.setRemoveAllWhenShown(true);
		mm.addMenuListener(
				mm2 -> {
					fillContextMenu(mm2);
					if (mm2.isEmpty()) {
						if (fEmptyMenuAction == null) {
							fEmptyMenuAction = new Action(Utilities.getString(fBundle, "emptyMenuItem")) { //$NON-NLS-1$
								// left empty
							};
							fEmptyMenuAction.setEnabled(false);
					}
						mm2.add(fEmptyMenuAction);
				}
			}
		);
		tree.setMenu(mm.createContextMenu(tree));
	}

	/**
	 * Returns the viewer's name.
	 *
	 * @return the viewer's name
	 */
	public String getTitle() {
		String title= Utilities.getString(fBundle, "title", null); //$NON-NLS-1$
		if (title == null)
			title= Utilities.getString("DiffTreeViewer.title"); //$NON-NLS-1$
		return title;
	}

	/**
	 * Returns the resource bundle.
	 *
	 * @return the viewer's resource bundle
	 */
	protected ResourceBundle getBundle() {
		return fBundle;
	}

	/**
	 * Returns the compare configuration of this viewer.
	 *
	 * @return the compare configuration of this viewer
	 */
	public CompareConfiguration getCompareConfiguration() {
		return fCompareConfiguration;
	}

	/**
	 * Called on the viewer disposal.
	 * Unregisters from the compare configuration.
	 * Clients may extend if they have to do additional cleanup.
	 *
	 * @param event dispose event that triggered call to this method
	 */
	@Override
	protected void handleDispose(DisposeEvent event) {
		if (fCompareConfiguration != null) {
			if (fPropertyChangeListener != null)
				fCompareConfiguration.removePropertyChangeListener(fPropertyChangeListener);
			fCompareConfiguration= null;
		}
		fPropertyChangeListener= null;

		super.handleDispose(event);
	}

	/**
	 * Tracks property changes of the configuration object.
	 * Clients may extend to track their own property changes.
	 * In this case they must call the inherited method.
	 *
	 * @param event property change event that triggered call to this method
	 */
	protected void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(CompareConfiguration.MIRRORED)) {
			diffViewerLabelProvider.fireLabelProviderChanged();
		}
	}

	@Override
	protected void inputChanged(Object in, Object oldInput) {
		super.inputChanged(in, oldInput);

		if (in != oldInput) {
			initialSelection();
			updateActions();
		}
	}

	/**
	 * This hook method is called from within {@code inputChanged}
	 * after a new input has been set but before any controls are updated.
	 * This default implementation calls {@code navigate(true)}
	 * to select and expand the first leaf node.
	 * Clients can override this method and are free to decide whether
	 * they want to call the inherited method.
	 *
	 * @since 2.0
	 */
	protected void initialSelection() {
		navigate(true);
	}

	/**
	 * Overridden to avoid expanding {@code DiffNode}s that shouldn't expand.
	 *
	 * @param node the node to expand
	 * @param level non-negative level, or {@code ALL_LEVELS} to collapse all levels of the tree
	 */
	@Override
	protected void internalExpandToLevel(Widget node, int level) {
		Object data= node.getData();

		if (dontExpand(data))
			return;

		super.internalExpandToLevel(node, level);
	}

	/**
	 * This hook method is called from within {@code internalExpandToLevel}
	 * to control whether a given model node should be expanded or not.
	 * This default implementation checks whether the object is a {@code DiffNode} and
	 * calls {@code dontExpand()} on it.
	 * Clients can override this method and are free to decide whether
	 * they want to call the inherited method.
	 *
	 * @param o the model object to be expanded
	 * @return {@code false} if a node should be expanded, {@code true} to prevent expanding
	 * @since 2.0
	 */
	protected boolean dontExpand(Object o) {
		return o instanceof DiffNode && ((DiffNode) o).dontExpand();
	}

	//---- merge action support

	/**
	 * This factory method is called after the viewer's controls have been created.
	 * It installs four actions in the given {@code ToolBarManager}. Two actions
	 * allow for copying one side of a {@code DiffNode} to the other side.
	 * Two other actions are for navigating from one node to the next (previous).
	 * <p>
	 * Clients can override this method and are free to decide whether they want to call
	 * the inherited method.
	 *
	 * @param toolbarManager the toolbar manager for which to add the actions
	 */
	protected void createToolItems(ToolBarManager toolbarManager) {
	}

	/**
	 * This method is called to add actions to the viewer's context menu.
	 * It installs actions for expanding tree nodes, copying one side of a {@code DiffNode} to the other side.
	 * Clients can override this method and are free to decide whether they want to call
	 * the inherited method.
	 *
	 * @param manager the menu manager for which to add the actions
	 */
	protected void fillContextMenu(IMenuManager manager) {
		if (fExpandAllAction == null) {
			fExpandAllAction= new Action() {
				@Override
				public void run() {
					expandSelection();
				}
			};
			Utilities.initAction(fExpandAllAction, fBundle, "action.ExpandAll."); //$NON-NLS-1$
		}

		boolean enable= false;
		ISelection selection= getSelection();
		if (selection instanceof IStructuredSelection) {
			Iterator<?> elements= ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object element= elements.next();
				if (element instanceof IDiffContainer) {
					if (((IDiffContainer) element).hasChildren()) {
						enable= true;
						break;
					}
				}
			}
		}
		fExpandAllAction.setEnabled(enable);
		manager.add(fExpandAllAction);
	}

	/**
	 * Expands to infinity all items in the selection.
	 *
	 * @since 2.0
	 */
	protected void expandSelection() {
		ISelection selection= getSelection();
		if (selection instanceof IStructuredSelection) {
			Iterator<?> elements= ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next= elements.next();
				expandToLevel(next, ALL_LEVELS);
			}
		}
	}

	/**
	 * Copies one side of all {@code DiffNode}s in the current selection to the other side.
	 * Called from the (internal) actions for copying the sides of a {@code DiffNode}.
	 * Clients may override.
	 *
	 * @param leftToRight if {@code true} the left side is copied to the right side.
	 *     If {@code false} the right side is copied to the left side
	 */
	protected void copySelected(boolean leftToRight) {
		ISelection selection= getSelection();
		if (selection instanceof IStructuredSelection) {
			Iterator<?> e= ((IStructuredSelection) selection).iterator();
			while (e.hasNext()) {
				Object element= e.next();
				if (element instanceof ICompareInput)
					copyOne((ICompareInput) element, leftToRight);
			}
		}
	}

	/**
	 * Called to copy one side of the given node to the other.
	 * This default implementation delegates the call to {@code ICompareInput.copy(...)}.
	 * Clients may override.
	 *
	 * @param node the node to copy
	 * @param leftToRight if {@code true} the left side is copied to the right side.
	 *     If {@code false} the right side is copied to the left side
	 */
	protected void copyOne(ICompareInput node, boolean leftToRight) {
		node.copy(leftToRight);

		// Update node's image.
		update(new Object[] { node }, null);
	}

	/**
	 * Selects the next (or previous) node of the current selection.
	 * If there is no current selection the first (last) node in the tree is selected.
	 * Wraps around at end or beginning.
	 * Clients may override.
	 *
	 * @param next if {@code true} the next node is selected, otherwise the previous node
	 */
	protected void navigate(boolean next) {
		// Fix for https://dev.eclipse.org/bugs/show_bug.cgi?id=20106
		internalNavigate(next, false);
	}

	//---- private

	/**
	 * Selects the next (or previous) node of the current selection.
	 * If there is no current selection the first (last) node in the tree is selected.
	 * Wraps around at end or beginning.
	 * Clients may override.
	 *
	 * @param next if {@code true} the next node is selected, otherwise the previous node
	 * @param fireOpen if {@code true} an open event is fired.
	 * @return {@code true} if at end (or beginning)
	 */
	private boolean internalNavigate(boolean next, boolean fireOpen) {
		Control c= getControl();
		if (!(c instanceof Tree) || c.isDisposed())
			return false;
		TreeItem item = getNextItem(next, true);
		if (item != null) {
			internalSetSelection(item, fireOpen);
		}
		return item == null;
	}

	private TreeItem getNextItem(boolean next, boolean expand) {
		Control c= getControl();
		if (!(c instanceof Tree) || c.isDisposed())
			return null;

		Tree tree= (Tree) c;
		TreeItem item= null;
		TreeItem children[]= tree.getSelection();
		if (children != null && children.length != 0)
			item= children[0];
		if (item == null) {
			children= tree.getItems();
			if (children != null && children.length != 0) {
				item= children[0];
				if (item != null && item.getItemCount() <= 0) {
					return item;
				}
			}
		}

		while (true) {
			item= findNextPrev(item, next, expand);
			if (item == null)
				break;
			if (item.getItemCount() <= 0)
				break;
		}
		return item;
	}

	private TreeItem findNextPrev(TreeItem item, boolean next, boolean expand) {
		if (item == null)
			return null;

		TreeItem children[]= null;

		if (!next) {
			TreeItem parent= item.getParentItem();
			if (parent != null) {
				children= parent.getItems();
			} else {
				children= item.getParent().getItems();
			}

			if (children != null && children.length > 0) {
				// Go to previous child.
				int index= 0;
				for (; index < children.length; index++) {
					if (children[index] == item)
						break;
				}

				if (index > 0) {
					item= children[index-1];

					while (true) {
						createChildren(item);
						int n= item.getItemCount();
						if (n <= 0)
							break;

						if (expand)
							item.setExpanded(true);
						item= item.getItems()[n-1];
					}

					// Previous.
					return item;
				}
			}

			// Go up.
			item= parent;
		} else {
			if (expand)
				item.setExpanded(true);
			createChildren(item);

			if (item.getItemCount() > 0) {
				// Has children: go down.
				children= item.getItems();
				return children[0];
			}

			while (item != null) {
				children= null;
				TreeItem parent= item.getParentItem();
				if (parent != null) {
					children= parent.getItems();
				} else {
					children= item.getParent().getItems();
				}

				if (children != null && children.length > 0) {
					// Goto next child.
					int index= 0;
					for (; index < children.length; index++) {
						if (children[index] == item)
							break;
					}

					if (index < children.length-1) {
						// Next.
						return children[index+1];
					}
				}

				// Go up.
				item= parent;
			}
		}

		return item;
	}

	private void internalSetSelection(TreeItem ti, boolean fireOpen) {
		if (ti != null) {
			Object data= ti.getData();
			if (data != null) {
				// Fix for https://dev.eclipse.org/bugs/show_bug.cgi?id=20106
				ISelection selection= new StructuredSelection(data);
				setSelection(selection, true);
				ISelection currentSelection= getSelection();
				if (fireOpen && currentSelection != null && selection.equals(currentSelection)) {
					fireOpen(new OpenEvent(this, selection));
				}
			}
		}
	}

	private void updateActions() {
		if (fExpandAllAction != null) {
			fExpandAllAction.setEnabled(getSelection().isEmpty());
		}
	}

	/*
	 * Fix for https://dev.eclipse.org/bugs/show_bug.cgi?id=20106
	 */
	private boolean internalOpen()  {
		ISelection selection= getSelection();
		if (selection != null && !selection.isEmpty()) {
			fireOpen(new OpenEvent(this, selection));
			return true;
		}
		return false;
	}


}
