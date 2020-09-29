/*******************************************************************************
 * Copyright (c) 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

/**
 * Section with two labels: a label for a property name, and a label for a
 * property value
 *
 */
public class LabeledPropertySection extends WizardPageSection {

	/// options
	private String propertyName; // what text to use for the label of the field
	private String tooltip = null; // what text to use for tooltip

	/// model elements
	private final LiveVariable<String> property;
	private final LiveExpression<ValidationResult> validator;
	private LiveExpression<Boolean> enabler = LiveExpression.constant(true);

	/// UI elements
	private Label propertyValueLabel;
	private ControlDecoration errorMarker;
	private final ValueListener<ValidationResult> errorMarkerListener = UIValueListener.from((exp, result) -> {
		if (errorMarker != null && errorMarker.getControl() != null && !errorMarker.getControl().isDisposed()) {
			if (result != null && result.status == IStatus.ERROR) {
				errorMarker.show();
			} else {
				errorMarker.hide();
			}
		}
	});

	//////////////////////////////

	public LabeledPropertySection(IPageWithSections owner,
			String propertyName,
			LiveVariable<String> property,
			LiveExpression<ValidationResult> validator) {
		super(owner);
		this.propertyName = propertyName;
		this.property = property;
		this.validator = validator;
	}

	public LabeledPropertySection(IPageWithSections owner, FieldModel<String> f) {
		this(owner, f.getLabel(), f.getVariable(), f.getValidator());
	}

	public LabeledPropertySection(IPageWithSections owner,
			String label,
			LiveVariable<String> variable
	) {
		this(owner, label, variable, Validator.OK);
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return validator;
	}

	@Override
	public void createContents(Composite page) {
		// project specification group
		Composite projectGroup = new Composite(page, SWT.NONE);
		GridLayout layout = GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 2).create();
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label propertyNameLabel = new Label(projectGroup, SWT.NONE);
		propertyNameLabel.setText(propertyName);
        GridDataFactory.fillDefaults()
        	.align(SWT.BEGINNING, SWT.CENTER)
        	.applyTo(propertyNameLabel);

		propertyValueLabel = new Label(projectGroup, SWT.BORDER);

		GridDataFactory.fillDefaults()
			.align(SWT.BEGINNING, SWT.CENTER)
			.grab(true, false)
			.applyTo(propertyValueLabel);
		
		errorMarker = new ControlDecoration(propertyValueLabel, SWT.TOP | SWT.RIGHT);

		Image errorImage = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage();

		errorMarker.setImage(errorImage);
		errorMarker.hide();

		if (validator != null) {
			validator.addListener(errorMarkerListener);
		}

		property.addListener(UIValueListener.from((exp, value) -> {
			if (propertyValueLabel != null && !propertyValueLabel.isDisposed()) {
				if (value == null) {
					value = "";
				}
				String oldText = propertyValueLabel.getText();
				if (!oldText.equals(value)) {
					propertyValueLabel.setText(value);
				}
			}
		}));
		enabler.addListener(UIValueListener.from((exp, enable) -> {
			propertyValueLabel.setEnabled(enable);
		}));
		if (tooltip != null) {
			propertyNameLabel.setToolTipText(tooltip);
			propertyValueLabel.setToolTipText(tooltip);
		}
	}

	public LabeledPropertySection tooltip(String string) {
		this.tooltip = string;
		return this;
	}

	public LabeledPropertySection setEnabler(LiveExpression<Boolean> enable) {
		this.enabler = enable;
		return this;
	}

	@Override
	public void dispose() {
		if (validator != null) {
			validator.removeListener(errorMarkerListener);
		}
		super.dispose();
	}
}
