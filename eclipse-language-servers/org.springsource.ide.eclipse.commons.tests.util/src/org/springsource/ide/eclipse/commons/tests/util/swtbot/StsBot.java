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
package org.springsource.ide.eclipse.commons.tests.util.swtbot;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.hamcrest.Matcher;

/**
 * @author Leo Dos Santos
 * @author Terry Denney
 */
public class StsBot extends SWTWorkbenchBot {

	public SWTBotButton flatButton(String mnemonicText) {
		return flatButton(mnemonicText, 0);
	}

	@SuppressWarnings("unchecked")
	public SWTBotButton flatButton(String mnemonicText, int index) {
		Matcher matcher = allOf(widgetOfType(Button.class), withMnemonic(mnemonicText), withStyle(SWT.FLAT, "SWT.FLAT"));
		return new SWTBotButton((Button) widget(matcher, index), matcher);
	}

	public SWTBotButton flatButtonInSection(String mnemonicText, String inSection) {
		return flatButtonInSection(mnemonicText, inSection, 0);
	}

	@SuppressWarnings("unchecked")
	public SWTBotButton flatButtonInSection(String mnemonicText, String inSection, int index) {
		Matcher matcher = allOf(widgetOfType(Button.class), withMnemonic(mnemonicText), InSection.inSection(inSection),
				withStyle(SWT.FLAT, "SWT.FLAT"));
		return new SWTBotButton((Button) widget(matcher, index), matcher);
	}

	public SWTBotHyperlink hyperlink(String mnemonicText) {
		return hyperlink(mnemonicText, 0);
	}

	@SuppressWarnings("unchecked")
	public SWTBotHyperlink hyperlink(String mnemonicText, int index) {
		Matcher matcher = allOf(widgetOfType(Hyperlink.class), withMnemonic(mnemonicText));
		return new SWTBotHyperlink((Hyperlink) widget(matcher, index), matcher);
	}

	@SuppressWarnings("unchecked")
	public SWTBotRadio radioInSection(String mnemonicText, String inSection) {
		Matcher matcher = allOf(widgetOfType(Button.class), withMnemonic(mnemonicText), InSection.inSection(inSection),
				withStyle(SWT.RADIO, "SWT.RADIO"));
		return new SWTBotRadio((Button) widget(matcher, 0), matcher);
	}

	public SWTBotTable tableInSection(String inSection) {
		return tableInSection(inSection, 0);
	}

	@SuppressWarnings("unchecked")
	public SWTBotTable tableInSection(String inSection, int index) {
		Matcher matcher = allOf(widgetOfType(Table.class), InSection.inSection(inSection));
		return new SWTBotTable((Table) widget(matcher, index), matcher);
	}

	public SWTBotTree treeInSection(String inSection) {
		return treeInSection(inSection, 0);
	}

	@SuppressWarnings("unchecked")
	public SWTBotTree treeInSection(String inSection, int index) {
		Matcher matcher = allOf(widgetOfType(Tree.class), InSection.inSection(inSection));
		return new SWTBotTree((Tree) widget(matcher, index), matcher);
	}

}
