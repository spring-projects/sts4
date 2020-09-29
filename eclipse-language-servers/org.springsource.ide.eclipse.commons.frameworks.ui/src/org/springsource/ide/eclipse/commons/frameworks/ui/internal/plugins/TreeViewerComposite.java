/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.plugins;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Basic tree viewer that creates N number of columns based on the columns
 * specified by the caller as well as initialises the columns
 * @author Nieraj Singh
 */
public class TreeViewerComposite {

	private TreeViewer viewer;

	private IPluginListColumn[] columns;
	private ColumnSortListener columnListener;
	private IPluginListColumn defaultSortColumn;

	public TreeViewerComposite(IPluginListColumn[] columns,
			IPluginListColumn defaultSortColumn) {
		this.defaultSortColumn = defaultSortColumn;
		this.columns = columns;
	}

	protected int getViewerConfiguration() {
		return SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL;
	}

	protected ICheckStateProvider getCheckStateProvider() {
		return null;
	}

	public TreeViewer getTreeViewer() {
		return viewer;
	}

	protected int getViewerHeightHint() {
		return 200;
	}

	public void createControls(Composite parent) {

		Composite treeComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(treeComposite);
		GridLayoutFactory.fillDefaults().applyTo(treeComposite);

		Tree tree = new Tree(treeComposite, getViewerConfiguration());

		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT,
				getViewerHeightHint()).applyTo(tree);

		viewer = new TreeViewer(tree);

		if (columns != null && columns.length > 0) {
			PixelConverter converter = new PixelConverter(treeComposite);
			for (IPluginListColumn column : columns) {
				if (column != null) {

					TreeColumn treeColumn = new TreeColumn(tree, SWT.NONE);
					treeColumn.setResizable(true);

					treeColumn.setWidth(converter
							.convertWidthInCharsToPixels(column.getWidth()));
					treeColumn.setText(column.getName());
				}
			}
			tree.setHeaderVisible(true);
		}

		initSorting();
		initListeners();

		viewer.refresh();

	}

	protected void initSorting() {
		TreeColumn sortColumn = getDefaultSortColumn();
		Tree tree = viewer.getTree();
		if (sortColumn != null) {
			tree.setSortColumn(sortColumn);
			tree.setSortDirection(SWT.UP);
		}
	}

	protected TreeColumn getDefaultSortColumn() {
		if (defaultSortColumn == null) {
			return null;
		}

		String sortColumnName = defaultSortColumn.getName();

		if (sortColumnName == null) {
			return null;
		}

		Tree tree = viewer.getTree();
		TreeColumn[] columns = tree.getColumns();
		if (columns != null) {

			for (TreeColumn column : columns) {
				if (sortColumnName.equals(column.getText())) {
					return column;
				}
			}
		}
		return null;
	}
	
	protected void initListeners() {
		TreeColumn[] columns = viewer.getTree().getColumns();
		if (columnListener != null) {
			removeListeners();
		}
		columnListener = new ColumnSortListener();
		for (TreeColumn column : columns) {
			column.addSelectionListener(columnListener);
		}
	}

	public void dispose() {
		removeListeners();
	}

	protected void removeListeners() {

		if (columnListener != null) {
			TreeColumn[] columns = viewer.getTree().getColumns();

			for (TreeColumn column : columns) {
				column.removeSelectionListener(columnListener);
			}
		}
	}

	protected class ColumnSortListener extends SelectionAdapter {


		public void widgetSelected(SelectionEvent e) {
			if (e.widget instanceof TreeColumn) {
				TreeColumn selectedColumn = (TreeColumn) e.widget;
				Tree tree = viewer.getTree();
				TreeColumn currentSortColumn = tree.getSortColumn();
				// If it is the same column as the sort, change direction
				int newDirection = SWT.UP;
				if (currentSortColumn != selectedColumn) {
					tree.setSortColumn(selectedColumn);

				} else {
					newDirection = tree.getSortDirection() == SWT.UP ? SWT.DOWN
							: SWT.UP;
				}
				tree.setSortDirection(newDirection);
				viewer.refresh();
			}
		}

	}

}
