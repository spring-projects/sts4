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

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * @author Terry Denney
 */
public class StsBotTestUtil {

	public static void showView(String category, String view, SWTBot bot) {
		bot.menu("Window").menu("Show View").menu("Other...").click();

		SWTBotShell shell = bot.shell("Show View");
		shell.activate();

		bot.tree().expandNode(category).select(view);
		bot.button("OK").click();
	}

}
