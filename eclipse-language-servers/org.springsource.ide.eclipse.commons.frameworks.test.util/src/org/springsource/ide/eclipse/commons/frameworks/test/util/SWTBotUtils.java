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
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withRegex;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.waitForShell;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.ContextMenuFinder;
import org.eclipse.swtbot.swt.finder.finders.MenuFinder;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.utils.TableCollection;
import org.eclipse.swtbot.swt.finder.utils.TableRow;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.waits.WaitForObjectCondition;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PlatformUI;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;


/**
 * Utility methods that are useful in writing SWTBot tests.
 * @author Kris De Volder
 * @author Nieraj Singh
 * @author Tomasz Zarna
 */
public class SWTBotUtils {

	public static final long PROCESS_COMPLETION_TIMEOUT = 60000; //Needs enough time to finish running grails command like compile -non-interactive
	public static final long WAIT_FOR_BUILD_TIMEOUT = 10000;

	/**
	 * Open a particular perspective, or try to activate it if it can't be
	 * opened (maybe it already open)
	 */
	public static void openPerspective(SWTWorkbenchBot bot,
			String perspectiveLabel) {
		SWTBotShell shell = null;
		try {
			menu(bot, "Window").menu("Open Perspective").menu("Other...")
					.click();

			shell = bot.shell("Open Perspective");
			// SWTBotUtils.screenshot("openPerspective_"+perspectiveLabel);
			assertTrue(shell.isOpen());
			bot.waitUntil(SWTBotUtils.widgetMakeActive(shell));
			shell.bot().table().select(perspectiveLabel);

			shell.bot().button("OK").click();
			bot.waitUntil(Conditions.shellCloses(shell));
		} catch (Exception e) {
			if (shell != null && shell.isOpen())
				shell.close();
			System.err.println("Couldn't open perspective '" + perspectiveLabel
					+ "'\n"
					+ "trying to activate already open perspective instead");
			// maybe somehow the perspective is already opened (by another test
			// before us)
			SWTBotPerspective perspective = bot
					.perspectiveByLabel(perspectiveLabel);
			perspective.activate();
			assertTrue(perspective.isActive());
		}
		Assert.assertEquals(perspectiveLabel, bot.activePerspective()
				.getLabel());
	}
	
	/**
	 * @return
	 */
	public static Shell getMainShell() {
		Shell mainWidget = UIThreadRunnable.syncExec(new Result<Shell>() {
			public Shell run() {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getShell();
			}
		});
		return mainWidget;
	}

	/**
	 * Create an SWTBot {@link ICondition} that tests whether a given resource
	 * exists.
	 * 
	 * @param pathToResource
	 *           Path starting from workspace root. For example
	 *           "kris/src/Foo.java" would refer to file "Foo.java" in the
	 *           "src" directory of project "kris". Path's ending with trailing
	 *           "/" will be considered to point to "IFolder" path without
	 *           trailing "/" will be treated as "IFile" references. Paths with
	 *           no "/" at all will be treated as references to a project.
	 * @return
	 */
	public static ResourceExists resourceExists(String pathToResource) {
		return new ResourceExists(pathToResource);
	}

	/**
	 * Wait for resource to be created.
	 * 
	 * @param pathToResource
	 *           Path starting from workspace root. For example
	 *           "kris/src/Foo.java" would refer to file "Foo.java" in the
	 *           "src" directory of project "kris". Path's ending with trailing
	 *           "/" will be considered to point to "IFolder" path without
	 *           trailing "/" will be treated as "IFile" references. Paths with
	 *           on "/" at all will be treated as references to a project.
	 */
	public static void waitResourceCreated(SWTBot bot, String pathToResource,
			long timeOut) {
		bot.waitUntil(resourceExists(pathToResource), timeOut);
	}

	/**
	 * Wait for resource to be created.
	 * 
	 * @param pathToResource
	 *           Path starting from workspace root. For example
	 *           "kris/src/Foo.java" would refer to file "Foo.java" in the
	 *           "src" directory of project "kris". Path's ending with trailing
	 *           "/" will be considered to point to "IFolder" path without
	 *           trailing "/" will be treated as "IFile" references. Paths with
	 *           on "/" at all will be treated as references to a project.
	 */
	public static void waitResourceCreated(SWTBot bot, String pathToResource) {
		waitResourceCreated(bot, pathToResource, SWTBotPreferences.TIMEOUT);
	}

	/**
	 * @return a List of {@link Shell} under a parent.
	 * @param parent
	 *           the parent under which a shell will be found.
	 */
	public static List<Shell> shells(SWTBotShell parent) {
		Matcher<Shell> anyShell = widgetOfType(Shell.class);
		WaitForObjectCondition<Shell> waitForShell = waitForShell(anyShell,
				parent.widget);
		parent.bot().waitUntilWidgetAppears(waitForShell);
		List<Shell> allShells = waitForShell.getAllMatches();

		System.err.println(">>> SWTBot Shells found:");
		for (Shell shell : allShells) {
			SWTBotShell bot = new SWTBotShell(shell);
			System.err.println("   '" + bot.getText() + "'");
		}
		System.err.println("<<< SWTBot Shells found:");

		return allShells;
	}

	/**
	 * @return the first shell that is present or appears under a given parent.
	 * @param parent
	 *           the parent under which the shell will be found.
	 */
	public static SWTBotShell shell(SWTBotShell parentShell) {
		return new SWTBotShell(shells(parentShell).get(0));
	}

	/**
	 * Wait for a shell with the given regular expression.
	 * 
	 * @param bot the SWTBot
	 * @param regex the regular expression
	 */
	public static void waitForShellWithRegex(SWTBot bot, String regex) {
		Matcher<Shell> withRegex = withRegex(regex);
		WaitForObjectCondition<Shell> waitForShell = waitForShell(withRegex);
		bot.waitUntilWidgetAppears(waitForShell);
	}

	public static void doubleClick(SWTWorkbenchBot bot,
			final SWTBotTable table, int row, int col) {
		table.click(row, col);
		// Note: table.doubleClick() method doesn't work, table appears not to
		// be listening to the events that are being posted by this method.
		// Use this workaround instead:
		// https://dev.eclipse.org/mhonarc/newsLists/news.eclipse.swtbot/msg00309.html
		UIThreadRunnable.asyncExec(bot.getDisplay(), new VoidResult() {
			public void run() {
				table.widget.notifyListeners(SWT.DefaultSelection, new Event());
			}
		});
	}

	/**
	 * A better version of the "menu" method from SWTBot. This one will retry to
	 * get the active shell on each poll, rather than get the active shell at
	 * the start and then get stuck using the wrong shell.
	 */
	public static SWTBotMenu menu(SWTWorkbenchBot bot, String name) {
		return new SWTBotMenu(SWTBotConditions.waitForShellMenuList(bot, name,
				true).get(0));
	}

	/**
	 * Given a view name containing a tree, this method will attempt to select a
	 * node contained within a parent.
	 * 
	 * @param bot
	 *           containing shell with a tree
	 * @param shellName
	 *           of the shell containing tree
	 * @param parentName
	 *           parent containing the node to be selected
	 * @param childName
	 *           node to be selected
	 * @return the selected node
	 */
	public static SWTBotTreeItem selectChildTreeElement(SWTBot bot,
			String shellName, String parentName, String childName) {
		SWTBotShell shell = bot.shell(shellName);
		shell.activate();
		SWTBotTreeItem parentNode = SWTBotConditions.waitForNodeExpanded(bot,
				parentName);
		SWTBotTreeItem childNode = SWTBotConditions.waitChildNodeToAppear(bot,
				parentNode, childName);
		return childNode.select();
	}

	/**
	 * Gets a view by name. It assumes the view MUST be opened, although not
	 * necessarily active. Open the view first if necessary.
	 * 
	 * @return Active, focused explorer view
	 */
	public static SWTBotView getView(SWTWorkbenchBot bot, String name) {
		SWTBotView explorer = bot.viewByTitle(name);
		explorer.setFocus();
		return explorer;
	}

	/**
	 * Selects a project in the currently opened view. A view must be opened
	 * prior to using this method.
	 * 
	 * @param projectName
	 * @return
	 */
	public static SWTBotTree selectProject(SWTWorkbenchBot bot,
			String projectName, String viewName) {

		SWTBotView explorer = getView(bot, viewName);
		SWTBotTree tree = explorer.bot().tree();
		if (projectName != null) {
			tree.select(projectName);
			waitForSelection(bot, projectName, tree);
			return tree;
		} else {
			tree.select(new String[0]);
			waitForSelection(bot, "", tree);
			return tree;
		}
	}

	/**
	 * Sometimes widgets respond slowly to selection and key presses. Use this
	 * method to wait for a selection to become what you expect it to be.
	 * Otherwise, trying to proceed will probably fail the test.
	 */
	public static void waitForSelection(SWTWorkbenchBot bot,
			final String expected, final SWTBotList list) {
		bot.waitUntil(new DefaultCondition() {
			public boolean test() throws Exception {
				String selection = getSelection();
				return expected.equals(selection);
			}

			private String getSelection() {
				String[] selections = list.selection();
				String selection = null;
				if (selections.length == 1)
					selection = selections[0];
				return selection;
			}

			public String getFailureMessage() {
				return "Was expecting selection '" + expected + " but found '"
						+ getSelection() + "'";
			}
		});
	}

	/**
	 * Wait for a selection to become what you expect it to be. This method
	 * assumes a SWTBotTree that only has a single column selection.
	 */
	public static void waitForSelection(SWTWorkbenchBot bot,
			final String expected, final SWTBotTree tree) {
		bot.waitUntil(new DefaultCondition() {
			public boolean test() throws Exception {
				String selection = getSelection(tree);
				return expected.equals(selection);
			}

			public String getFailureMessage() {
				return "Was expecting selection \n----\n" + expected
						+ "\n----\nbut found \n----\n" + getSelection(tree)
						+ "\n-----";
			}
		});
	}

	public static String getSelection(SWTBotTree tree) {
		TableCollection selections = tree.selection();
		String selection = "";
		for (int r = 0; r < selections.rowCount(); r++) {
			if (r > 0)
				selection += "\n";
			TableRow row = selections.get(r);
			for (int c = 0; c < row.columnCount(); c++) {
				if (c > 0)
					selection += " >> ";
				selection += row.get(c);
			}
		}
		return selection;
	}

	private static int screenshotNumber = 0;

	/**
	 * Call this method to create extra screenshots along the way. A message
	 * will be printed to System.err when the screenshot is taken (look in error
	 * log to see which screenshot file was taken when). Scrreenshots will
	 * include a sequence number at the start of their name that makes it easy
	 * to see in what order they were taken.
	 * 
	 * @filename The screenshot will be save as "screenshots/filename_<number>"
	 */
	public static void screenshot(String fileName) {
		fileName = "screenshots/" + screenshotNumber() + fileName + "."
				+ SWTBotPreferences.SCREENSHOT_FORMAT.toLowerCase();
		System.err.println("Creating extra screenshot: " + fileName);
		new File("screenshots").mkdirs(); //$NON-NLS-1$
		SWTUtils.captureScreenshot(fileName);
	}

	private static String screenshotNumber() {
		return String.format("%03d_", screenshotNumber++);
	}
	
	public static void waitForAllBuildJobs(SWTBot bot){
		waitForAllProcessesToTerminate(bot);
		waitForJobCompletions();
		bot.sleep(WAIT_FOR_BUILD_TIMEOUT);
		waitForJobCompletions();
	}
	
	private static void waitForJobCompletions() {
		StsTestUtil.waitForAutoBuild();
		StsTestUtil.waitForManualBuild();
		StsTestUtil.waitForJobFamily(ResourcesPlugin.FAMILY_AUTO_REFRESH);
		StsTestUtil.waitForJobFamily(ResourcesPlugin.FAMILY_MANUAL_REFRESH);
	}
	
	public static void waitForAllProcessesToTerminate(SWTBot bot) {
		waitForAllProcessesToTerminate(bot, PROCESS_COMPLETION_TIMEOUT);
	}

	public static void waitForAllProcessesToTerminate(SWTBot bot, long timeout) {
		bot.waitUntil(new ICondition() {

			private IProcess activeProcess = null;

			public boolean test() throws Exception {
				IProcess[] processes = DebugPlugin.getDefault().getLaunchManager().getProcesses();
				for (IProcess process : processes) {
					if (!process.isTerminated()) {
						print("Waiting for processes...");
						print("active = "+process.getClass()+ " " + process.getLabel());
						activeProcess = process;
						return false;
					}
				}
				print("Waiting for processes... DONE");
				return true;
			}

			public void init(SWTBot bot) {
				
			}

			public String getFailureMessage() {
				return "A process is still active:" +
					   "\n    label = " + activeProcess.getLabel() +
					   "\n    type  = " + activeProcess.getAttribute(IProcess.ATTR_PROCESS_TYPE) +
					   "\n    cmd   = " + activeProcess.getAttribute(IProcess.ATTR_CMDLINE);
			}
			
		}, timeout);
	}

	/**
	 * This is only for debugging purposes, in case having trouble finding the
	 * menus...
	 */
	public static List<MenuItem> findAllContextMenus(Shell shell) {
		MenuFinder menuFinder = new ContextMenuFinder(shell);
		return menuFinder.findMenus(shell, anyMenuMatcher, true);
	}

	private static final Matcher<MenuItem> anyMenuMatcher = new BaseMatcher<MenuItem>() {

		public boolean matches(Object item) {
			boolean result = item instanceof MenuItem;
			if (result) {
				MenuItem menu = (MenuItem) item;
				System.out.println("Found menu with text : '" + menu.getText()
						+ "'");
			}
			return result;
		}

		public void describeTo(Description description) {
			description.appendText("Any menu");
		}
	};

	/**
	 * @param explorerTree
	 * @return
	 */
	public static ICondition widgetMakeActive(
			final AbstractSWTBot<? extends Widget> widget) {
		return new ICondition() {
			public boolean test() throws Exception {
				widget.setFocus();
				return widget.isActive();
			}

			public void init(SWTBot bot) {
			}

			public String getFailureMessage() {
				return "Widget not active: " + widget;
			}
		};
	}

	private static boolean enablePrinting = true;

	public static void enablePrinting() {
		enablePrinting = true;
	}

	public static void disablePrinting() {
		enablePrinting = false;
	}

	public static void print(String message) {
		if (enablePrinting) {
			System.out.println(message);
		}
	}

	public static ICondition widgetIsDisposed(
			AbstractSWTBot<? extends Widget> widget) {
		return new WidgetIsDisposed(widget);
	}

	public static String getConsoleText(SWTWorkbenchBot bot) {
		SWTBotView console = bot.viewByTitle("Console");
		SWTBotStyledText textWidget = console.bot().styledText();
		return textWidget.getText();
	}

	/**
	 * Repeatedly try to find a submenu that contains a given String,
	 * until the menu appears, or until timeout happens.
	 */
	public static SWTBotMenu subMenuContaining(SWTBot bot, SWTBotMenu parentMenu, String text) {
		Matcher<MenuItem> matcher = SWTBotConditions.withTextContaining(text);
		SubMenusMatching condition = new SubMenusMatching(bot, parentMenu, matcher);
		return new SWTBotMenu(condition.getMenus().get(0));
	}

}
