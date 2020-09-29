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
package org.springsource.ide.eclipse.commons.tests.util;

import java.io.File;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.commons.net.CommonsNetPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.utils.ClassUtils;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Prints the name of each test to System.err when it started and dumps a stack
 * trace of all thread to System.err if a test takes longer than 10 minutes.
 * @author Steffen Pingel
 * @author Kris De Volder
 */
public class ManagedTestSuite extends TestSuite {

	private class DumpThreadTask extends TimerTask {

		private final Test test;

		private final Thread testThread;

		public DumpThreadTask(Test test, Thread testThread) {
			this.test = test;
			this.testThread = testThread;
		}

		private void dumpJobs() {
			StringBuffer sb = new StringBuffer();
			sb.append(MessageFormat.format("Jobs:\n", test.toString()));
			Job[] jobs = Job.getJobManager().find(null);
			for (Job job : jobs) {
				sb.append(job.getName().toString());
				sb.append(" [");
				sb.append(JobManager.printState(job.getState()));
				sb.append(", ");
				sb.append(job.getClass().getName());
				sb.append("]");
				sb.append("\n");
			}
			System.err.println(sb.toString());
		}

		@Override
		public void run() {
			// dump all thread for diagnosis
			StringBuffer sb = StsTestUtil.getStackDumps();
			System.err.println(
					MessageFormat.format("Test {0} is taking too long:\n", test.toString()) +
					sb.toString());
			dumpJobs();

//			killTest("Test is taking too long");

			// capture screenshot for diagnosis
			final String fileName = "screenshots/screenshot-" + ClassUtils.simpleClassName(test.getClass()) + "." //$NON-NLS-1$ //$NON-NLS-2$
					+ SWTBotPreferences.SCREENSHOT_FORMAT.toLowerCase();
			File screenshotFile = new File("screenshots");
			System.err.println("Captured screenshot to " + screenshotFile.getAbsolutePath());
			screenshotFile.mkdirs();
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					// This deadlocks when run in UI thread!
					SWTUtils.captureScreenshot(fileName);
				}
			});

			// attempt to close any modal dialogs
			if (test instanceof ShutdownWatchdog) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						if (window != null) {
							Shell shell = window.getShell();
							Shell[] shells = window.getShell().getDisplay().getShells();
							for (Shell child : shells) {
								if (child != shell) {
									child.close();
								}
							}
						}
					}
				});
			}
		}

		@SuppressWarnings("deprecation")
		private void killTest(String debugInfo) {
			try {
				// Yes, the stop method is deprecated, but I don't know another
				// way to attempt to stop a runaway test without the cooperation
				// of the test/thread itself. This may not work as desired in
				// all cases, but is almost certainly better than leaving the
				// "stuck" test hanging.
				System.err.println("[TIMEOUT] " + test);
				testThread.stop();
			}
			catch (Throwable e) {
				e.printStackTrace();
			}
		}
	};

	private class Listener implements TestListener {

		private DumpThreadTask task;

		private Timer timer = new Timer(true);

		public void addError(Test test, Throwable t) {
			System.err.println("[ERROR]");
		}

		public void addFailure(Test test, AssertionFailedError t) {
			System.err.println("[FAILURE]");
		}

		private void dumpList(String header, Enumeration<TestFailure> failures) {
			System.err.println(header);
			while (failures.hasMoreElements()) {
				TestFailure failure = failures.nextElement();
				System.err.print("  ");
				System.err.println(failure.toString());
			}
		}

		public void dumpResults(TestResult result) {
			System.err.println();
			dumpList("Failures: ", result.failures());

			System.err.println();
			dumpList("Errors: ", result.errors());

			int failedCount = result.errorCount() + result.failureCount();
			System.err.println();
			System.err.println(MessageFormat.format("{0} out of {1} tests failed", failedCount, result.runCount()));
		}

		public void endTest(Test test) {
			if (task != null) {
				task.cancel();
				task = null;
			}
		}

		public void startTest(Test test) {
			Thread testThread = Thread.currentThread();
			System.err.println("Running " + test.toString());
			task = new DumpThreadTask(test, testThread);
			try {
				timer.scheduleAtFixedRate(task, DELAY, DELAY);
			} catch (IllegalStateException e) {
				//No idea where, who or why, but timer gets 'canceled'.
				// We'll need a new one
				timer = new Timer(true);
				timer.scheduleAtFixedRate(task, DELAY, DELAY);
			}
		}

	}

	public class ShutdownWatchdog implements Test {

		public int countTestCases() {
			return 1;
		}

		public void run(TestResult result) {
			// do nothing
		}

		@Override
		public String toString() {
			return "ShutdownWatchdog";
		}

	}

	public long DELAY = 10 * 60 * 1000;

	private final Listener listener = new Listener();

	public ManagedTestSuite() {
	}

	public ManagedTestSuite(String name) {
		super(name);
	}

	@Override
	public void run(TestResult result) {
		result.addListener(listener);
		dumpSystemInfo();
		super.run(result);
		listener.dumpResults(result);

		// add dummy test to dump threads in case shutdown hangs
		listener.startTest(new ShutdownWatchdog());
	}

	private void dumpSystemInfo() {
		try {
			if (Platform.isRunning() && CommonsNetPlugin.getProxyService() != null
					&& CommonsNetPlugin.getProxyService().isSystemProxiesEnabled()
					&& !CommonsNetPlugin.getProxyService().hasSystemProxies()) {
				// XXX e3.5/gtk.x86_64 activate manual proxy configuration which
				// defaults to Java system properties if system proxy support is
				// not available
				System.err.println("Forcing manual proxy configuration");
				CommonsNetPlugin.getProxyService().setSystemProxiesEnabled(false);
				CommonsNetPlugin.getProxyService().setProxiesEnabled(true);
			}

			Properties p = System.getProperties();
			if (Platform.isRunning()) {
				p.put("build.system", Platform.getOS() + "-" + Platform.getOSArch() + "-" + Platform.getWS());
			}
			else {
				p.put("build.system", "standalone");
			}
			String info = "System: ${os.name} ${os.version} (${os.arch}) / ${build.system} / ${java.vendor} ${java.vm.name} ${java.version}";
			for (Entry<Object, Object> entry : p.entrySet()) {
				info = info.replaceFirst(Pattern.quote("${" + entry.getKey() + "}"), entry.getValue().toString());
			}
			System.err.println(info);
			System.err.print("Proxy : " + WebUtil.getProxy("google.com", IProxyData.HTTP_PROXY_TYPE) + " (Platform)");
			try {
				System.err.print(" / " + ProxySelector.getDefault().select(new URI("https://google.com")) + " (Java)");
			}
			catch (URISyntaxException e) {
				// ignore
			}
			System.err.println();
			System.err.println();
		}
		catch (ConcurrentModificationException e) {
			// Not sure why but sometimes thrown by the code that is dumping out
			// system properties!
			// Catch and print it, but don't abort the test runner simply
			// because this info can't be dumped.
			e.printStackTrace();
		}
	}

}
