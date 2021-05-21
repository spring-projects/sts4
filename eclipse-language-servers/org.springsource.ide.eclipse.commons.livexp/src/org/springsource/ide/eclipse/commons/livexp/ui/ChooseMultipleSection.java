/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

public class ChooseMultipleSection<T extends Ilabelable> extends WizardPageSection {

	private static final boolean DEBUG = false; //(""+Platform.getLocation()).contains("kdvolder");

	private String labelText;
	private Ilabelable[] validChoices;
	private LiveSet<T> chosen;
	private LiveExpression<ValidationResult> validator;
	private LiveVariable<T> selected;
	private boolean vertical;

	public ChooseMultipleSection(IPageWithSections owner,
			String labelText,
			T[] validChoices,
			LiveSet<T> chosen,
			LiveExpression<ValidationResult> validator,
			LiveVariable<T> selected
	) {
		super(owner);
		this.labelText = labelText;
		this.validChoices = validChoices;
		this.chosen = chosen;
		this.validator = validator;
		this.selected = selected;
	}
	
	public ChooseMultipleSection(IPageWithSections owner,
			String labelText,
			T[] validChoices,
			LiveSet<T> chosen,
			LiveExpression<ValidationResult> validator
	) {
		this(owner, labelText, validChoices, chosen, validator, null);
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return validator;
	}
	
	public  ChooseMultipleSection<T> vertical(boolean vertical) {
		this.vertical = vertical;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void createContents(Composite page) {
        Composite composite = new Composite(page, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = vertical ? 1 : 2;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        GridDataFactory grab = GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, UIConstants.SCROLLABLE_LIST_HEIGTH);
        grab.applyTo(composite);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(composite, SWT.NONE);
		label.setText(labelText);
		GridDataFactory.fillDefaults()
			.align(SWT.BEGINNING, SWT.BEGINNING)
			.hint(vertical ? SWT.DEFAULT : UIConstants.fieldLabelWidthHint(label), SWT.DEFAULT)
			.applyTo(label);

		CheckboxTableViewer tv = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		grab.applyTo(tv.getTable());
		tv.setContentProvider(new ContentProvider());
		tv.setLabelProvider(new LabelProvider());
		tv.setInput(validChoices);
		tv.setCheckedElements(chosen.getValues().toArray());

		tv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				@SuppressWarnings("unchecked")
				T e = (T) event.getElement();
				boolean checked = event.getChecked();
				if (checked) {
					chosen.add(e);
				} else {
					chosen.remove(e);
				}
			}
		});
		
		if (selected != null) {
			tv.addSelectionChangedListener(event -> {
				IStructuredSelection structuredSelection = event.getStructuredSelection();
				if (structuredSelection.isEmpty()) {
					selected.setValue(null);
				} else {
					selected.setValue((T)structuredSelection.getFirstElement());
				}
			});
		}

		chosen.addListener(new ValueListener<Set<T>>() {
			public void gotValue(LiveExpression<Set<T>> exp, Set<T> value) {
				Set<T> values = exp.getValue();
				tv.setCheckedElements(values.toArray());
				if (DEBUG) {
					System.out.println(">>>> starters");
					for (T e : value) {
						System.out.println(e.getLabel());
					}
					System.out.println("<<<< starters");
				}
			}
		});

	}

	class ContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public Object[] getElements(Object inputElement) {
			return validChoices;
		}
	}

	public class LabelProvider implements ILabelProvider {

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Image getImage(Object element) {
			//no image
			return null;
		}

		public String getText(Object element) {
			if (element instanceof Ilabelable) {
				return ((Ilabelable) element).getLabel();
			}
			return ""+element;
		}
	}
}
