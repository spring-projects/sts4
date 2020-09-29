/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.test.util;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

public class Timewatch {

	/**
	 * Runs a chunk of code on the current thread, giving a limited amount of time.
	 * After the time limit is exceed a thread-dump is created on sysout.
	 */
	public static void monitor(String taskName, Duration limit, Asserter doit) throws Exception {
		AtomicBoolean done = new AtomicBoolean(false);
		Job threadDumper = new Job("ThreadDumper") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (!done.get()) {
					System.err.println("WARNING! "+taskName+ " is longer than "+(limit.toMillis() / 1000)+" seconds");
					System.err.println(StsTestUtil.getStackDumps());
				}
				return Status.OK_STATUS;
			}
		};
		threadDumper.schedule(limit.toMillis());
		try {
			doit.execute();
		} finally {
			done.set(true);
			threadDumper.cancel();
		}
	}
	
}
