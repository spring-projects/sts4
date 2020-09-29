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

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.SWTBotTestCase;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;


/**
 * @author Steffen Pingel
 * @author Terry Denney
 */
public abstract class StsUiTestCase extends SWTBotTestCase {

	protected StsBot bot;

	private final String defaultKbLayout = SWTBotPreferences.KEYBOARD_LAYOUT;

	@After
	public void after() throws Exception {
		cleanUp();
	}

	@Before
	public void before() throws Exception {
		setUp();
	}

	protected void cleanUp() throws Exception {
		List<? extends SWTBotEditor> editors = bot.editors();
		for (SWTBotEditor editor : editors) {
			editor.close();
		}

		StsTestUtil.deleteAllProjects();

		Shell mainWidget = UIThreadRunnable.syncExec(new Result<Shell>() {
			public Shell run() {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			}
		});
		SWTBotShell[] shells = bot.shells();
		SWTBotShell mainShell = null;
		for (SWTBotShell shell : shells) {
			if (shell.widget != mainWidget) {
				shell.close();
			}
			else {
				mainShell = shell;
			}
		}
		if (mainShell != null) {
			mainShell.activate();
		}
	}

	@Override
	public void setUp() throws Exception {
		try {
			SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
			bot = new StsBot();
			try {
				bot.viewByTitle("Welcome").close();
			}
			catch (WidgetNotFoundException e) {
				// ignore
			}

			// run in setUp() to enable super class to capture screenshot in
			// case of a failure
			cleanUp();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		SWTBotPreferences.KEYBOARD_LAYOUT = defaultKbLayout;
		super.tearDown();
	}

}
