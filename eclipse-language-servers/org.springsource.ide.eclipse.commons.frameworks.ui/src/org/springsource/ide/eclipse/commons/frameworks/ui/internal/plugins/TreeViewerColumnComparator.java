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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Basic column comparator that compares elements in a tree based on the String value of
 * the elements.
 * @author Nieraj Singh
 */
public class TreeViewerColumnComparator extends ViewerSorter {

	public int compare(Viewer viewer, Object e1, Object e2) {
		if (viewer instanceof TreeViewer) {
			Tree tree = ((TreeViewer) viewer).getTree();

			TreeColumn sortColumn = tree.getSortColumn();
			int sortDirection = tree.getSortDirection();
			if (sortColumn != null) {
				String compareText1 = getCompareString(sortColumn, e1);
				String compareText2 = getCompareString(sortColumn, e2);
				if (compareText1 != null) {
					if (compareText2 != null) {

						return sortDirection == SWT.UP ? compareText1
								.compareToIgnoreCase(compareText2)
								: compareText2
										.compareToIgnoreCase(compareText1);
					} else {
						return sortDirection == SWT.UP ? -1 : 1;
					}
				} else if (compareText2 != null) {
					return sortDirection == SWT.UP ? 1 : -1;
				}

			}
		}

		return super.compare(viewer, e1, e2);
	}

	protected String getCompareString(TreeColumn column, Object rowItem) {
		if (rowItem != null) {
			return rowItem.toString();
		}
		return null;
	}

}
