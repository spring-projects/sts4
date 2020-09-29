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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameter;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameterDescriptor;


/**
 * @author Nieraj Singh
 * @author Christian Dupuis
 */
public class BooleanParameterEditor extends AbstractParameterEditor implements
		IParameterEditor {

	private Button boolButton;

	public BooleanParameterEditor(ICommandParameter command) {
		super(command, false);
	}

	public Composite createControls(Composite parent) {
		Composite baseCommandArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0)
				.applyTo(baseCommandArea);
		GridDataFactory.fillDefaults().grab(false, false)
				.applyTo(baseCommandArea);

		final ICommandParameterDescriptor parameter = getParameterDescriptor();

		String parameterName = parameter.getName();

		// Use the any set value. Typically should always be set to the default
		// value
		Object defaultValue = getParameter().getValue();
		boolean valueToSet = (defaultValue instanceof Boolean) ? ((Boolean) defaultValue)
				.booleanValue() : false;

		boolButton = new Button(baseCommandArea, SWT.CHECK);
		// Note that the widget text may change if the wizard adds mneumonics
		// for keyboard shortcuts
		// therefore store the actual name as data as well and rely on the
		// widget data as opposed
		// to the text to get the parameter name
		boolButton.setText(parameterName);
		boolButton.setData(parameterName);
		boolButton.setSelection(valueToSet);
		boolButton.setToolTipText(parameter.getDescription());

		GridDataFactory.fillDefaults().grab(false, false).applyTo(boolButton);

		boolButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				setParameterValueAndNotifyClear(new Boolean(boolButton
						.getSelection()));
			}

		});
		return baseCommandArea;
	}

	protected void clearControls() {
		if (boolButton != null && !boolButton.isDisposed()) {
			boolButton.setSelection(false);
		}
	}

	public Button getButtonControl() {
		return boolButton;
	}
}
