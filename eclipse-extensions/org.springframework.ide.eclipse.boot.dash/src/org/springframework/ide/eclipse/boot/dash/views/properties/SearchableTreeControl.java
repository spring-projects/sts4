/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.springsource.ide.eclipse.commons.livexp.core.FilterBoxModel;
import org.springsource.ide.eclipse.commons.livexp.ui.util.SwtConnect;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;
import org.springsource.ide.eclipse.commons.livexp.util.Filters;

/**
 * Tree viewer control with search box that searches for any text pattern in the
 * tree. This control is specifically meant for a page container.
 *
 * <p/>
 *
 * Will switch between a simple message label control and the searchable tree
 * control if no content is found on refresh. Therefore a no-content message
 * supplier is required.
 *
 * @author Alex Boyko
 * @author Nieraj Singh
 *
 */
public class SearchableTreeControl {

	private TreeViewer treeViewer;
	private Text searchBox;
	private Composite treeViewerComposite;

	private final FormToolkit widgetFactory;

	/**
	 *
	 * @param widgetFactory
	 * @param missingContentSupplier supplies a message if no content for tree is
	 *                               available. Supplier MUST return null if content
	 *                               is available.
	 */
	public SearchableTreeControl(FormToolkit widgetFactory) {
		this.widgetFactory = widgetFactory;
	}

	public void createControls(Composite parent, ITreeContentProvider treeContentProvider, LabelProvider labelProvider) {

		treeViewerComposite = new Composite(parent, SWT.NONE);
		treeViewerComposite
				.setLayout(GridLayoutFactory.fillDefaults().margins(0, 0).spacing(1, 1).numColumns(1).create());

		searchBox = widgetFactory.createText(treeViewerComposite, "",
				SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		searchBox.setMessage("Enter search string");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(searchBox);
		FilterBoxModel<String> searchBoxModel = new FilterBoxModel<String>() {
			@Override
			protected Filter<String> createFilterForInput(String pattern) {
				return Filters.caseInsensitiveSubstring(pattern);
			}
		};
		SwtConnect.connect(searchBox, searchBoxModel.getText());
		searchBox.addDisposeListener(de -> searchBoxModel.close());

		treeViewer = new TreeViewer(treeViewerComposite /* , SWT.NO_SCROLL */);

		treeViewer.setContentProvider(treeContentProvider);
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setAutoExpandLevel(2);

		SwtConnect.connectTextBasedFilter(treeViewer, searchBoxModel.getFilter(), labelProvider, treeContentProvider);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(treeViewer.getTree());

	}

	public Composite getComposite() {
		return treeViewerComposite;
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	public void refresh() {
		// If tree is populated for the first time then auto expand to level 2 manually
		// because new input is not set in this case
		boolean firstTimeTreePopulated = treeViewer.getTree().getItems().length == 0;
		treeViewer.refresh();

		if (firstTimeTreePopulated) {
			treeViewer.expandToLevel(2);
		}
	}
}
