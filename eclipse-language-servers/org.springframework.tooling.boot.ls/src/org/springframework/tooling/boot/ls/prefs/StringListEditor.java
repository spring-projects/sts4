/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.prefs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.widgets.Composite;

public class StringListEditor extends ListEditor {

	private static final String DELIMITER = "---";
	
	private String promptTitle;
	private String promptMessage;
	private IInputValidator validator;
	
	public static String encode(String[] items) {
		return String.join(DELIMITER, items);
	}
	
	public static String[] decode(String value) {
		if (value.isEmpty()) {
			return new String[0];
		}
		return value.split(DELIMITER);
	}

	@Override
	protected String createList(String[] items) {
		return encode(items);
	}
	
	public StringListEditor(Composite parent, String name, String label, String promptTitle, String promptMessage, IInputValidator validator) {
		super(name, label, parent);
		this.promptTitle = promptTitle;
		this.promptMessage = promptMessage;
		this.validator = validator;
	}
	
	@Override
	protected String getNewInputObject() {
		InputDialog dialog = new InputDialog(getShell(), promptTitle, promptMessage, "", validator);
		if (dialog.open() == IDialogConstants.OK_ID) {
			return dialog.getValue();
		}
		return null;
	}

	@Override
	protected String[] parseString(String stringList) {
		return decode(stringList);
	}

}
