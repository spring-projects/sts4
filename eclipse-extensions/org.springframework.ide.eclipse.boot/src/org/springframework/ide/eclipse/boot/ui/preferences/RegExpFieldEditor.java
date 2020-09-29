/*******************************************************************************
 *  Copyright (c) 2015 Pivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui.preferences;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * StringFieldEditor which validates its input by parsing as a java.util.Pattern.
 *
 * @author Kris De Volder
 */
public class RegExpFieldEditor extends StringFieldEditor {

	public RegExpFieldEditor(String name, String label, Composite parent) {
		super(name, label, parent);
		setErrorMessage(label+":  PatternSyntaxException");
	}

	@Override
	protected boolean checkState() {
		Text text = getTextControl();
		if (text == null) {
			return false;
		}
		String patternString = text.getText();
		try {
			Pattern.compile(patternString);
			clearErrorMessage();
			return true;
		} catch (PatternSyntaxException e) {
			showErrorMessage();
			return false;
		}
	}

}
