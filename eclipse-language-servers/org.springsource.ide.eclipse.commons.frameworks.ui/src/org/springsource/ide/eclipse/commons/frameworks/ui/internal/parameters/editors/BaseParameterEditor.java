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
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameter;


/**
 * Creates an editor with a 2 column composite, where the first column has a
 * text control and the second allows for further controls in that same row
 * (i.e. by having a subclass add a nested composite in the second column with
 * additional controls that appear in that same row).
 * @author Nieraj Singh
 * @author Christian Dupuis
 */
public class BaseParameterEditor extends AbstractParameterEditor {

	private Text commandValueText;
	boolean firstFocus = true;
	protected static final int NUMBER_OF_COLUMNS = 2;

	private int uiEventType = UIEvent.VALUE_SET | UIEvent.CLEAR_VALUE_EVENT;

	public BaseParameterEditor(ICommandParameter parameter,
			boolean requiresLabel) {
		super(parameter, requiresLabel);
	}

	protected int numberOfTextControlCompositeColumns() {
		return NUMBER_OF_COLUMNS;
	}

	public Composite createControls(Composite parent) {

		Composite textControlComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults()
				.numColumns(numberOfTextControlCompositeColumns())
				.margins(0, 0).applyTo(textControlComposite);
		GridDataFactory.fillDefaults().grab(true, false)
				.applyTo(textControlComposite);

		commandValueText = new Text(textControlComposite, SWT.BORDER);

		commandValueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		// Set the default value BEFORE adding the modify listener
		Object defaultValue = getParameter().getValue();
		if (defaultValue instanceof String) {
			commandValueText.setText((String) defaultValue);
		}

		commandValueText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				notifyTextChange(commandValueText.getText());
			}
		});

		commandValueText.addFocusListener(new FocusListener() {

			public void focusLost(FocusEvent e) {
				// Nothing
			}

			public void focusGained(FocusEvent e) {
				// Notify the wizard that focus has been set the first
				// time to allow the wizard to output any error messages
				if (firstFocus) {
					notifyTextChange(commandValueText.getText());
					firstFocus = false;
				}
			}
		});

		return textControlComposite;
	}

	/**
	 * Text control for the base parameter. It is assumed that the text control
	 * uses GridLayout and GridData
	 * 
	 * @return text control, or null if it hasn't been created. Users should
	 *        check null/disposed condition when invoking this method
	 */
	protected Text getTextControl() {
		return commandValueText;
	}

	protected void notifyTextChange(String text) {
		setParameterValue(text, uiEventType);
		// Reset the event type
		uiEventType = UIEvent.VALUE_SET | UIEvent.CLEAR_VALUE_EVENT;
	}

	protected void clearControls() {
		if (commandValueText != null && !commandValueText.isDisposed()) {
			uiEventType = UIEvent.VALUE_SET;
			commandValueText.setText("");
		}
	}
}
