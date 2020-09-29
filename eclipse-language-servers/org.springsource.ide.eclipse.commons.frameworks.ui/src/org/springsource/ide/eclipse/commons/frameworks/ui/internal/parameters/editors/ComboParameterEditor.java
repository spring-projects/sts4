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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.parameters.editors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ComboParameterDescriptor;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameter;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameterDescriptor;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ParameterKind;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.swt.SWTFactory;


/**
 * @author Nieraj Singh
 */
public class ComboParameterEditor extends AbstractParameterEditor {

	public static final int DEFAULT_COMBO_BOX_LIMIT = 5;
	protected Map<String, Button> radialButtons;
	protected Combo combo;

	public ComboParameterEditor(ICommandParameter parameter,
			boolean requiresLabel) {
		super(parameter, requiresLabel);
	}

	public Composite createControls(Composite parent) {
		ICommandParameterDescriptor parameter = getParameterDescriptor();
		if (parameter == null
				|| parameter.getParameterKind() != ParameterKind.COMBO) {
			return null;
		}

		ComboParameterDescriptor comboParameter = (ComboParameterDescriptor) parameter;

		String[] values = comboParameter.getSelectionValues();
		

		if (values == null || values.length == 0) {
			return null;
		}

		int size = values.length;
		
		if (size <= getComboBoxLimit()) {
			createRadialButtons(parent, comboParameter);
		} else {
			createCombo(parent, comboParameter);
		}

		// based on the number of arguments create either a combo or a dropdown
		return null;
	}

	protected int getComboBoxLimit() {
		return DEFAULT_COMBO_BOX_LIMIT;
	}

	protected Control createCombo(Composite parent,
			ComboParameterDescriptor parameter) {

		String[] values = parameter.getSelectionValues();

		combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(combo);

		combo.setItems(values);

		String defaultValue = (String) getParameter().getValue();
		if (defaultValue != null) {
			for (int i = 0; i < values.length; i++) {
				if (defaultValue.equals(values[i])) {
					combo.select(i);
					break;
				}
			}
		}

		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setParameterValueAndNotifyClear(combo.getItem(combo
						.getSelectionIndex()));
			}
		});

		return combo;
	}

	protected Control createRadialButtons(Composite parent,
			ComboParameterDescriptor parameter) {

		Composite buttonArea = new Composite(parent, SWT.NONE);

		GridLayoutFactory.fillDefaults().numColumns(getComboBoxLimit())
				.equalWidth(true).applyTo(buttonArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonArea);
		radialButtons = new HashMap<String, Button>();
		String[] values = parameter.getSelectionValues();

		// if there is a default value, set it
		String defaultValue = (String) getParameter().getValue();
		for (String value : values) {
			final Button button = SWTFactory.createRadialButton(buttonArea,
					value, false);

			if (button != null) {
				button.setData(value);

				if (value.equals(defaultValue)) {
					button.setSelection(true);
				}
				button.addSelectionListener(new SelectionAdapter() {

					public void widgetSelected(SelectionEvent e) {
						setParameterValueAndNotifyClear(button.getData());
					}

				});
				radialButtons.put(value, button);
			}
		}
		return buttonArea;
	}

	protected void clearControls() {
		if (combo != null && !combo.isDisposed()) {
			combo.deselectAll();
		} else if (radialButtons != null) {
			Collection<Button> buttons = radialButtons.values();
			for (Button button : buttons) {
				if (!button.isDisposed()) {
					button.setSelection(false);
				}
			}
		}
	}

}
