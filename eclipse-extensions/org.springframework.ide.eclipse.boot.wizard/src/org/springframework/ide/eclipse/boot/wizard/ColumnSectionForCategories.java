/*******************************************************************************
 * Copyright (c) 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.SelectionModel;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.SimpleLabelProvider;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;

/**
 * This is one of the columns in a three-column version of the Spring starter
 * wizard: column 1 is the category selection section.
 * 
 *
 *
 */
@Deprecated
public class ColumnSectionForCategories extends WizardPageSection {

	private SelectionModel<String> selection;
	private String[] categories; //The elements to choose from
	private final NewSpringBootWizardModel model;

	private TableViewer viewer;
	private ILabelProvider labelProvider = new SimpleLabelProvider();
	List<String> hiddenCategories = new ArrayList<>();
	
	public ColumnSectionForCategories(IPageWithSections owner, NewSpringBootWizardModel model, SelectionModel<String> selection) {
		super(owner);
		this.selection = selection;
		this.model = model;
		this.categories = model.dependencies.getCategories().toArray(new String[]{});
	
		model.getDependencyFilter().addListener(new UIValueListener<Filter<Dependency>>() {
			@Override
			protected void uiGotValue(
					LiveExpression<Filter<Dependency>> exp,
					Filter<Dependency> value
			) {
				applyFilter(value);
			}

		});
	
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return selection.validator;
	}
	
	@Override
	public void createContents(Composite page) {
		Composite area = area(page);

		viewer = new TableViewer(area, SWT.SINGLE|SWT.BORDER|SWT.V_SCROLL);
		viewer.addFilter(new ChoicesFilter());
		viewer.setLabelProvider(labelProvider);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(categories);
		
		GridDataFactory grab = GridDataFactory.fillDefaults().grab(true, true);
		grab.applyTo(viewer.getTable());
		

		whenVisible(viewer.getControl(), () -> setSelectionInViewer());

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = viewer.getSelection();
				 if (sel instanceof IStructuredSelection){
					IStructuredSelection ss = (IStructuredSelection) sel;
					String val = (String)ss.getFirstElement();
					if (val != null) {
						selection.selection.setValue(val);
					}
				}
			}
		});
	}
	
	protected Composite area(Composite parent) {

		Composite area = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(area);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(area);

		return area;
	}
	
	protected void setSelectionInViewer() {
		if (viewer != null && !viewer.getControl().isDisposed()) {
			String preSelect = selection.selection.getValue();
			if (preSelect != null) {
				viewer.setSelection(new StructuredSelection(preSelect), true);
			} else {
				viewer.setSelection(StructuredSelection.EMPTY, true);
			}
		}
	}

	private void whenVisible(final Control control, final Runnable runnable) {
		PaintListener l = new PaintListener() {
			public void paintControl(PaintEvent e) {
				runnable.run();
				control.removePaintListener(this);
			}
		};
		control.addPaintListener(l);
	}
	
	private void applyFilter(Filter<Dependency> filter) {
		hiddenCategories.clear();
		if (viewer != null && !viewer.getControl().isDisposed()) {
			
			
			for (String cat : model.dependencies.getCategories()) {
			
				MultiSelectionFieldModel<Dependency> dependencyGroup = model.dependencies.getContents(cat);
				List<CheckBoxModel<Dependency>> checkBoxModels = dependencyGroup.getCheckBoxModels();
				boolean showCategory = false;

				if (checkBoxModels !=  null) {
					for (CheckBoxModel<Dependency> checkBoxModel : checkBoxModels) {
						// If filter applies to at least one item, show the category
						if (applyFilterOnItem(filter, checkBoxModel)) {
							showCategory = true;
							break;
						}
					}
				}
				
				if (!showCategory) {
					hiddenCategories.add(cat);
				}
				
			}
			setSelectionInViewer();
			viewer.refresh(true);
		}
	}
	
	private boolean applyFilterOnItem(Filter<Dependency> filter, CheckBoxModel<Dependency> model) {
		return filter.accept(model.getValue());
	}
	
	private class ChoicesFilter extends ViewerFilter {

		public ChoicesFilter() {

		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			String label = labelProvider.getText(element);
			if (match(label)) {
				return true;
			}
			return false;
		}

		private boolean match(String categoryLabel) {
			return !hiddenCategories.contains(categoryLabel);
		}
	}
}
