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

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.hamcrest.Matchers.allOf;

import java.util.List;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.MenuFinder;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.hamcrest.Matcher;

/**
 * Find submenus of a given parent menu, matching a given condition. If none are found,
 * wait/retry until timeout happens (to handle cases where menus are dynamically generated
 * and may only appear after some time.
 * @author Kris De Volder
 */
public class SubMenusMatching extends AbstractedWaitCondition {
	private Menu parentMenu;
	private boolean recursive;
	private List<MenuItem> found;
	private Matcher<MenuItem> matcher;

	public SubMenusMatching(SWTBot bot, final SWTBotMenu parentMenuBot,
			Matcher<MenuItem> matcher) {
		super(bot);
		this.parentMenu = UIThreadRunnable.syncExec(new Result<Menu>() {
			public Menu run() {
				return parentMenuBot.widget.getMenu();
			}
		});
		this.matcher = matcher;
	}

	protected MenuFinder getMenuFinder() {
		return new MenuFinder();
	}

	public boolean test() throws Exception {
		MenuFinder finder = getMenuFinder();
		SWTBotUtils.print("Getting submenus for "+ parentMenu);

		@SuppressWarnings("unchecked")
		Matcher<MenuItem> menuMatcher = allOf(widgetOfType(MenuItem.class), matcher);
		found = finder.findMenus(parentMenu, menuMatcher, recursive);

		boolean hasFound = found != null && found.size() > 0;
		SWTBotUtils.print("Has found menus: " + hasFound + " for: " + matcher);
		return hasFound;
	}

	public List<MenuItem> getMenus() {
		waitForTest();
		return found;
	}

	public void init(SWTBot bot) {
		this.bot = bot;
	}

	public String getFailureMessage() {
		return "Failed to find menus for " + matcher + " under " +parentMenu;
	}
}
