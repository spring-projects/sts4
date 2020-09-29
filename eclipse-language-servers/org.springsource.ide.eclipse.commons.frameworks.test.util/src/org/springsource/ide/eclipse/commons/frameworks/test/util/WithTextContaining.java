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
package org.springsource.ide.eclipse.commons.frameworks.test.util;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.matchers.AbstractMatcher;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.hamcrest.Description;

/**
 * Matches widgets if the getText() method of the widget contains the specified text
 * as a substring.
 * @author Kris De Volder
 */
public class WithTextContaining<T extends Widget> extends AbstractMatcher<T> {

	protected String	text;

	public WithTextContaining(String text) {
		this.text = text;
	}

	protected boolean doMatch(Object obj) {
		try {
			String foundText =  getText(obj);
			SWTBotUtils.print("Found text = "+foundText);
			return foundText.contains(text);
		} catch (Exception e) {
			// do nothing
		}
		return false;
	}

	static String getText(Object obj) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return ((String) SWTUtils.invokeMethod(obj, "getText")).replaceAll(Text.DELIMITER, "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void describeTo(Description description) {
		description.appendText("with text containig'").appendText(text).appendText("'"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
