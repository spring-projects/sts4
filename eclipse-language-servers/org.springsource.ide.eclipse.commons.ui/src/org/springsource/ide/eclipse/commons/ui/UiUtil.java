/*******************************************************************************
 * Copyright (c) 2012, 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.internal.ui.UiPlugin;

/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Terry Denney
 */
@SuppressWarnings("restriction")
public class UiUtil {

	// shared colors for all forms
	private static FormColors formColors;

	public static final int WEB_BROWSER_EXTERNAL = WebBrowserPreference.EXTERNAL;

	public static void busyCursorWhile(final ICoreRunnable coreRunner) throws OperationCanceledException, CoreException {
		try {
			IRunnableWithProgress runner = new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						coreRunner.run(monitor);
					}
					catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
					finally {
						monitor.done();
					}
				}

			};
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runner);
		}
		catch (InvocationTargetException e) {
			if (e.getCause() instanceof CoreException) {
				throw (CoreException) e.getCause();
			}
			else {
				StatusHandler.log(new Status(IStatus.ERROR, UiPlugin.PLUGIN_ID, "Unexpected exception", e));
			}
		}
		catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
	}

	public static FormColors getFormColors(Display display) {
		if (formColors == null) {
			formColors = new FormColors(display);
			formColors.markShared();
		}
		return formColors;
	}

	public static Shell getShell() {
		return Display.getDefault().getActiveShell();
	}

	public static IViewPart getViewFromActivePerspective(String id) {
		if (PlatformUI.isWorkbenchRunning()) {
			IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (activePage != null) {
				return activePage.findView(id);
			}
		}
		return null;
	}

	public static void openUrl(final String location) {
		// Must be running in the UI thread to open a browser and *surprisingly*
		// also to call getBrowserChoice. Otherwise a invalid thread access
		// exception may
		// ensue on windows and mac os and then internal browser is permanently
		// disabled.
		// This only happens sometimes.
		// maybe it depends on if the preferences have been initialized before.
		// In any case we need the asyncExec here!!!
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (display != null && !display.isDisposed()) {
			display.asyncExec(new Runnable() {
				public void run() {
					uiThreadOpenUrl(location, WebBrowserPreference.getBrowserChoice());
				}
			});
		}
	}

	/**
	 * Don't call this method unless you are in the UI thread!
	 */
	private static void uiThreadOpenUrl(String location, int browserChoice) {
		try {
			URL url = null;
			if (location != null) {
				url = new URL(location);
			}
			if (browserChoice == WebBrowserPreference.EXTERNAL) {
				try {
					IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
					support.getExternalBrowser().openURL(url);
				}
				catch (Exception e) {
				}
			}
			else {
				IWebBrowser browser;
				int flags;
				if (WorkbenchBrowserSupport.getInstance().isInternalWebBrowserAvailable()) {
					flags = IWorkbenchBrowserSupport.AS_EDITOR | IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}
				else {
					flags = IWorkbenchBrowserSupport.AS_EXTERNAL | IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}

				String generatedId = "org.eclipse.mylyn.web.browser-" + Calendar.getInstance().getTimeInMillis();
				browser = WorkbenchBrowserSupport.getInstance().createBrowser(flags, generatedId, null, null);
				browser.openURL(url);
			}
		}
		catch (PartInitException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Failed to Open Browser",
					"Browser could not be initiated");
		}
		catch (MalformedURLException e) {
			if (location == null || location.trim().equals("")) {
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Failed to Open Browser",
						"No URL to open." + location);
			}
			else {
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Failed to Open Browser",
						"Could not open URL: " + location);
			}
		}
	}

	public static void runForked(final ICoreRunnable coreRunner) throws OperationCanceledException, CoreException {
		runForked(coreRunner, PlatformUI.getWorkbench().getProgressService());
	}

	public static void runForked(final ICoreRunnable coreRunner, IRunnableContext progressService)
			throws OperationCanceledException, CoreException {
		try {
			IRunnableWithProgress runner = new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("", IProgressMonitor.UNKNOWN);
					try {
						coreRunner.run(monitor);
					}
					catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
					finally {
						monitor.done();
					}
				}

			};
			progressService.run(true, true, runner);
		}
		catch (InvocationTargetException e) {
			if (e.getCause() instanceof CoreException) {
				throw (CoreException) e.getCause();
			}
			else {
				StatusHandler.log(new Status(IStatus.ERROR, UiPlugin.PLUGIN_ID, "Unexpected exception", e));
			}
		}
		catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
	}

}
