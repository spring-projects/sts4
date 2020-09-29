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

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.MenuFinder;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.hamcrest.Matcher;

/**
 * @author Kris De Volder
 * @author Nieraj Singh
 * @author Steffen Pingel
 * @created Jul 8, 2010
 */
public class SWTBotConditions {

	public static final long PROJECT_ERROR_MARKER_TIMEOUT = 30000;
	public static final long CHILD_NODE_POPULATE_TIMEOUT = 60000;

	private SWTBotConditions() {
		// factor util
	}

	public static <T extends Widget> org.hamcrest.Matcher<T> withTextContaining(String text) {
		return new WithTextContaining<T>(text);
	}

	public static SWTBotTreeItem waitForNodeExpanded(SWTBot bot, String nodeName) {
		TreeNodeExpander expander = new TreeNodeExpander(bot);
		return expander.expand(nodeName);
	}

	public static List<MenuItem> waitForShellMenuList(SWTBot bot, String name,
			boolean recursive) {
		return new ActiveShellMenu(bot, name, recursive).getMenus();
	}

	public static SWTBotTreeItem waitChildNodeToAppear(SWTBot bot,
			final SWTBotTreeItem parentNode, final String nodeName) {
		final SWTBotTreeItem[] child = new SWTBotTreeItem[1];
		bot.waitUntil(new ICondition() {

			public boolean test() throws Exception {
				child[0] = parentNode.getNode(nodeName);
				return child[0] != null;
			}

			public void init(SWTBot bot) {
				//
			}

			public String getFailureMessage() {
				return "Unable to find child node: " + nodeName
						+ " for parent node: " + parentNode.getText();
			}

		}, CHILD_NODE_POPULATE_TIMEOUT);

		return child[0];
	}

	public static void waitForProjectErrorMarkersCleared(SWTBot bot,
			String projectName) {
		new ProjectErrorMarkersCleared(bot, projectName)
				.waitForTest(PROJECT_ERROR_MARKER_TIMEOUT);
	}

	protected static class TreeNodeExpander extends AbstractedWaitCondition {

		protected TreeNodeExpander(SWTBot bot) {
			super(bot);
		}

		private SWTBotTreeItem nodeToExpand;

		public boolean test() throws Exception {
			boolean expanded = nodeToExpand.isExpanded();
			SWTBotUtils.print("Tree item: " + nodeToExpand.getText()
					+ " is expanded: " + expanded);
			return expanded;
		}

		public String getFailureMessage() {
			return "Tree Node is not expanded or populated";
		}

		public SWTBotTreeItem expand(String nodeName) {
			nodeToExpand = bot.tree().expandNode(nodeName);
			waitForTest();
			SWTBotUtils.print("Has expanded: " + nodeName + " in shell "
					+ bot.activeShell().getText());
			return nodeToExpand;
		}
	}

	protected static class ActiveShellMenu extends AbstractedWaitCondition {

		protected SWTBot bot;
		private String name;
		private boolean recursive;
		private List<MenuItem> found;

		public ActiveShellMenu(SWTBot bot, String name, boolean recursive) {
			super(bot);
			this.name = name;
			this.recursive = recursive;
		}

		protected MenuFinder getMenuFinder() {
			return new MenuFinder();
		}

		public boolean test() throws Exception {
			MenuFinder finder = getMenuFinder();
			SWTBotUtils.print("Getting menus for shell: "
					+ bot.activeShell().getText());
			SWTBotUtils
					.print("Is active: " + bot.activeShell().isActive() + "");

			Matcher<MenuItem> menuMatcher = WidgetMatcherFactory
					.withMnemonic(name);
			Shell shell = bot.activeShell().widget;
			found = finder.findMenus(shell, menuMatcher, recursive);

			boolean hasFound = found != null && found.size() > 0;
			SWTBotUtils.print("Has found menus: " + hasFound + " for: " + name);
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
			return "Failed to find menus for " + name + " in shell: "
					+ bot.activeShell().getText();
		}
	}

	protected static class ProjectErrorMarkersCleared extends
			AbstractedWaitCondition {

		private String projectName;

		public ProjectErrorMarkersCleared(SWTBot bot, String projectName) {
			super(bot);
			this.projectName = projectName;
		}

		public boolean test() throws Exception {
			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectName);
			int markerSeverity = project.findMaxProblemSeverity(
					IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);

			if (markerSeverity >= IMarker.SEVERITY_ERROR) {
				SWTBotUtils.print("For project " + projectName
						+ " found max severity marker of " + markerSeverity
						+ ". Waiting for errors to clear.");
				return false;
			}
			return true;
		}

		public String getFailureMessage() {
			return "Condition failed because of error markers present in project children for project: "
					+ projectName;
		}

	}

	public static ICondition consoleContains(String string) {
		return new ConsoleContains(string);
	}

	public static class ConsoleContains implements ICondition {

		private SWTWorkbenchBot bot;
		private String searchString;
		private String msg;

		public ConsoleContains(String string) {
			this.searchString = string;
		}

		public boolean test() throws Exception {
			msg = "Could not open Console view";
			SWTBotView console = bot.viewByTitle("Console");
			msg = "Could not find textWidget in Console view";
			SWTBotStyledText textWidget = console.bot().styledText();
			msg = "Could not get the text from the Console view";
			String text = textWidget.getText();
			msg = "Looking for: '" +searchString+"' but found \n------\n"+ text+ "\n-----";
			return text.contains(searchString);
		}

		public void init(SWTBot bot) {
			this.bot = (SWTWorkbenchBot) bot;
		}

		public String getFailureMessage() {
			return msg;
		}

	}

	public static void waitForConsoleOutput(SWTWorkbenchBot bot, String string, long timeout) {
		bot.waitUntil(consoleContains(string), timeout);
	}

	public static void waitForConsoleOutput(SWTWorkbenchBot bot, String string) {
		bot.waitUntil(consoleContains(string));
	}

}
