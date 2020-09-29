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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.swt;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Part that adds a search text control where users can type a pattern that will
 * search for elements in a structured viewer based on the pattern. The viewer
 * will be filtered such that only elements that match the pattern are shown.
 * <p>
 * In order to properly initialise this part, the users must
 * <li>Create the part by specifying a parent where the text control should be
 * added</li>
 * <li>Connect to a structured viewer such that patterns entered in the text
 * control result in elements being shown in the viewer, and all other elements
 * filtered out</li>
 * </p>
 * Users must implement the matching conditions for each element in the viewer.
 * @author Nieraj Singh
 * @author Christian Dupuis
 */
public abstract class ViewerSearchPart {

	/**
	 * User appropriate getter to access the viewer
	 */
	private StructuredViewer viewer;

	private Text searchText;
	private String pattern;

	/**
	 * Parent must not be null. Text control will be added to the given parent
	 * as a child when this part is created.
	 * 
	 * @param parent
	 *           must not be null
	 */
	public ViewerSearchPart(Composite parent) {
		addPart(parent);
	}

	/**
	 * Adds the text control to the given composite. Must not be null.
	 * 
	 * @param parent
	 *           Cannot be null
	 */
	protected void addPart(Composite parent) {
		searchText = new Text(parent, SWT.SEARCH | SWT.ICON_CANCEL
				| SWT.ICON_SEARCH);
		searchText.setBackground(null);

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, false).applyTo(searchText);

		searchText.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent event) {
				handleSearch(searchText.getText());
			}

			public void keyPressed(KeyEvent event) {
				// nothing
			}
		});

		searchText.addSelectionListener(new SelectionAdapter() {

			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.detail == SWT.CANCEL) {
					String searchString = searchText.getText();
					handleSearch(searchString);
				}
			}
		});

		searchText.setMessage("Type pattern to match");

	}

	/**
	 * Connects the viewer search part to the given structured viewer, and
	 * creates the text search control such that search functionality is
	 * integrated into the given viewer. Note that the viewer and the parent
	 * Composite of this part need not be the same. The Text search control is
	 * added to the parent composite specified when this part was first created,
	 * but the specified viewer may not necessarily be in the same composite.
	 * <p>
	 * This decouples the area where the Text control is created with the area
	 * where the structured viewer is present.
	 * </p>
	 * 
	 * @param viewer
	 */
	public void connectViewer(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	protected StructuredViewer getViewer() {
		return viewer;
	}

	protected void handleSearch(String search) {
		refreshFilter(search);
	}

	abstract protected boolean matches(Object element, Object parentElement,
			String pattern);

	protected ViewerFilter getViewerFilter() {
		StructuredViewer viewer = getViewer();

		if (viewer == null) {
			return null;
		}
		ViewerFilter[] filters = viewer.getFilters();
		if (filters != null) {
			for (ViewerFilter filter : filters) {
				if (filter instanceof CommonSearchFilter) {
					return (CommonSearchFilter) filter;
				}
			}
		}
		return null;
	}

	protected void refreshFilter(String searchPattern) {
		StructuredViewer viewer = getViewer();

		if (viewer == null) {
			return;
		}
		pattern = searchPattern;
		ViewerFilter patternFilter = getViewerFilter();

		if (patternFilter != null) {
			if (pattern == null || isAllWhitespace(pattern)) {
				viewer.removeFilter(patternFilter);
			} else {
				viewer.refresh(false);
			}
		} else if (pattern != null && !isAllWhitespace(pattern)) {
			getViewer().addFilter(new CommonSearchFilter());
		}
	}

	/**
	 * Return active text control, or null if disposed or not created.
	 * 
	 * @return active text control for the search part, or null if disposed or
	 *        not created.
	 */
	public Text getTextControl() {
		return searchText != null && !searchText.isDisposed() ? searchText
				: null;
	}

	/**
	 * Filter used for text pattern filtering in the search area.
	 * 
	 * @author nisingh
	 * 
	 */
	protected class CommonSearchFilter extends ViewerFilter {

		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (pattern == null || isAllWhitespace(pattern)) {
				return false;
			}
			return matches(element, parentElement, pattern);
		}
	}

	public static boolean isAllWhitespace(String pattern) {
		if (pattern == null) {
			return false;
		}
		for (int i = 0; i < pattern.length(); i++) {
			if (Character.isWhitespace(pattern.charAt(i))) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}

}
