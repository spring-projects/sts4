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

import java.io.File;
import java.util.List;

import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBotTestCase;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;


/**
 * @author Kris De Volder
 * @author Nieraj Singh
 */
public class UITestCase extends SWTBotTestCase {

	private static Class<? extends UITestCase> isClassSetup = null;
	private static Class<? extends UITestCase> setupClassTried = null;
	protected static TestKeyboard keyboard;
	protected static SWTWorkbenchBot bot;
	protected static SWTBotShell mainShell;
	private static long defaultTimeOut;
	private static String defaultKeyboard;

	public static TestSuite createSuite(Class<? extends UITestCase> klass) {
		TestSuite suite = new TestSuite(klass);
		suite.addTest(TestSuite.createTest(klass, "tearDownClass"));
		return suite;
	}

	/**
	 * This method is made final, override setUpInstance and/or setupClass
	 * instead.
	 */
	@Override
	public final void setUp() {
		try {
			if (enablePrinting()) {
				SWTBotUtils.enablePrinting();;
			}
			if (!isClassSetup()) {
				try {
					setupClass();
				} finally {
					setupClassTried = this.getClass();
				}
				isClassSetup = this.getClass();
			}
			setupInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected boolean enablePrinting() {
		 return true;
	}

	protected void print(String message) {
		SWTBotUtils.print(message);
	}

	/**
	 * @return
	 */
	private boolean isClassSetup() {
		return this.getClass() == isClassSetup;
	}

	private boolean setupClassTried() {
		return this.getClass() == setupClassTried;
	}

	public void setupClass() throws Exception {
		bot = new SWTWorkbenchBot();
		mainShell = new SWTBotShell(SWTBotUtils.getMainShell());
		System.out.println("mainShell = " + mainShell);
		System.out.println("   with text= " + mainShell.getText());

		defaultTimeOut = SWTBotPreferences.TIMEOUT;
		// SWTBotPreferences.TIMEOUT = 30000; // a bit excessive? But some
		// grails commands take long time.
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US"; // In superclass
		keyboard = new TestKeyboard(bot);
		workspaceCleanUp(); // Get rid of artefacts left behind by other badly
							// behaved suites.
		workspaceFileSystemCleanup(); // Cleanup artefacts left behind
		                              // in the file system (out of synhc with Eclipse IResources)
		try {
			bot.viewByTitle("Welcome").close();
		} catch (WidgetNotFoundException e) {
		}

		if (!setupClassTried() && !StsTestUtil.isOnBuildSite()) {
			System.out.println("Deleting old screenshots...");
			File screenshotDir = new File("screenshots");
			File[] screenshots = screenshotDir.listFiles();
			if (screenshots != null) {
				for (int i = 0; i < screenshots.length; i++) {
					if (screenshots[i].toString().endsWith(".jpeg")) {
						screenshots[i].delete();
					}
				}
			}
		}
	}

	/**
	 * Create a general project in the workspace. To be used as test fixture.
	 *
	 * @return an IProject referring to the create project.
	 * @throws CoreException
	 */
	protected IProject createGeneralProject(String name) throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(name);
		if (!project.exists())
			project.create(null);
		if (!project.isOpen())
			project.open(null);
		return project;
	}

	/**
	 * General cleanup: close all editors. Delete all projects. Copied from:
	 * StsUiTestCase
	 *
	 * @author Steffen Pingel
	 */
	public static void workspaceCleanUp() throws CoreException {
		List<? extends SWTBotEditor> editors = bot.editors();
		for (SWTBotEditor editor : editors) {
			editor.close();
		}
		deleteAllProjects();
	}

	public static void deleteAllProjects() throws CoreException {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : allProjects) {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
			project.delete(true, true, null);
		}
	}

	private void workspaceFileSystemCleanup() {
		File workspace = StsTestUtil.getWorkspaceRoot().getLocation().toFile();
		File[] wsFiles = workspace.listFiles();
		System.out.println(">>>> wsFiles");
		for (File file : wsFiles) {
			System.out.println(file);
			if (file.toString().endsWith(".metadata")) {
				//Skip
			}
			else {
				deleteFiles(file);
			}
		}
		System.out.println("<<<< wsFiles");
	}

	private void deleteFiles(File file) {
		System.out.println("Deleting file: "+file);
		if (file.isDirectory()) {
			File[] subfiles = file.listFiles();
			for (File subfile : subfiles) {
				deleteFiles(subfile);
			}
		}
		file.delete();
	}

	private static void closeAllShells() {
		Shell mainWidget = SWTBotUtils.getMainShell();
		SWTBotShell[] shells = bot.shells();
		SWTBotShell mainShell = null;
		for (SWTBotShell shell : shells) {
			if (shell.widget != mainWidget) {
				shell.close();
			} else {
				mainShell = shell;
			}
		}
		if (mainShell != null) {
			mainShell.activate();
		}
	}



	public void setupInstance() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
		closeAllShells(); // crashed tests may leave open some windows / dialogs
							// interfering with the
							// other tests and causing cascading failures.
		super.tearDown();
	}

	/**
	 * Warning!!! Junit3 has no mechanism to ensure that this method is called.
	 * You can use the createSuite method provided in this class to make a suite
	 * that calls this method at the end of the suite.
	 * <p>
	 * For example, add the following code to your test Suite
	 *
	 * <code>
	 * class MySuite {
	 *   public static TestSuite suite() {
	 *       TestSuite suite = ...
	 *       ...
	 *       suiteAddTest(GrailsUITests.createSuite(MyTests.class));
	 *       ...
	 *   }
	 * }
	 * </code>
	 *
	 * @throws Exception
	 */
	public void tearDownClass() throws Exception {
		workspaceCleanUp();
		SWTBotPreferences.KEYBOARD_LAYOUT = defaultKeyboard;
		SWTBotPreferences.TIMEOUT = defaultTimeOut;
	}

	protected String getProjectViewCategory() {
		return "General";
	}

	protected String getProjectViewName() {
		return "Project Explorer";
	}

	/**
	 * Opens a particular view, from the Eclipse Window >> Show View menu.
	 */
	protected SWTBotView openView() {

		String category = getProjectViewCategory();
		String viewName = getProjectViewName();

		SWTBotView explorer = null;
		try {
			explorer = bot.viewByTitle(viewName);
			explorer.setFocus();
		}
		catch (Exception e) {
			e.printStackTrace();
			SWTBotUtils.menu(bot, "Window").menu("Show View").menu("Other...")
			.click();

			SWTBotUtils
			.selectChildTreeElement(bot, "Show View", category, viewName);

			bot.button("OK").click();
			explorer = getView();
		}
		assertTrue(explorer.isActive());
		return explorer;
	}

	/**
	 * Gets a view by name. It assumes the view MUST be opened, although not
	 * necessarily active. Open the view first if necessary.
	 *
	 * @return Active, focused explorer view
	 */
	protected SWTBotView getView() {
		return SWTBotUtils.getView(bot, getProjectViewName());
	}

	/**
	 * Gets a view by name. It assumes the view MUST be opened, although not necessarily active.
	 * Open the view first if necessary.
	 * @return Active, focused explorer view
	 */
	protected SWTBotView getView(String name) {
		return SWTBotUtils.getView(bot, name);
	}


	/**
	 * Sometimes widgets respond slowly to selection and key presses. Use this
	 * method to wait for a selection to become what you expect it to be.
	 * Otherwise, trying to proceed will probably fail the test.
	 */
	protected void waitForSelection(final String expected, final SWTBotList list) {
		SWTBotUtils.waitForSelection(bot, expected, list);
	}

	/**
	 * Wait for a selection to become what you expect it to be. This method
	 * assumes a SWTBotTree that only has a single column selection.
	 */
	protected void waitForSelection(final String expected, final SWTBotTree tree) {
		SWTBotUtils.waitForSelection(bot, expected, tree);
	}

	protected String getSelection(SWTBotTree tree) {
		return SWTBotUtils.getSelection(tree);
	}

	/**
	 * Activate a wizard from the "File >> New" menu.
	 *
	 * @param projectWizardName
	 * @param wizardShellName
	 * @return A reference to the Wizard's shell.
	 */
	protected SWTBotShell activateFileNewWizardShell(String projectWizardName,
			String wizardShellName) {
		SWTBotUtils.menu(bot, "File").menu("New").menu(projectWizardName)
				.click();

		SWTBotShell shell = bot.shell(wizardShellName);
		shell.activate();
		return shell;
	}

	/**
	 * Selects a project in the currently opened view. A view must be opened
	 * prior to using this method.
	 *
	 * @param projectName
	 * @return
	 */
	protected SWTBotTree selectProject(String projectName) {
		return SWTBotUtils.selectProject(bot, projectName, getProjectViewName());
	}

	protected Object getTreeItemObject(final SWTBotTreeItem item) {
		final Object[] result = new Object[1];
		UIThreadRunnable.syncExec(bot.getDisplay(), new VoidResult() {
			public void run() {
				result[0] = item.widget.getData();
			}
		});
		return result[0];
	}

	/**
	 * Gets the explorer tree in a currently OPENED explorer view. The explorer
	 * view MUST be opened before invoking this method
	 *
	 * @return explorer view tree
	 */
	protected SWTBotTree getExplorerViewTree() {
		SWTBotView explorer = getView();
		return explorer.bot().tree();
	}

}
