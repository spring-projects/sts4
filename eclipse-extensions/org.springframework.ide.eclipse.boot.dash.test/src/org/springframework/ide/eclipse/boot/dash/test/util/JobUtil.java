/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class JobUtil {

	public static <T> Future<T> runInJob(Callable<T> work) {
		CompletableFuture<T> done = new CompletableFuture<>();
		Job job = new Job("Backgorund Work") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					done.complete(work.call());
				} catch (Throwable e) {
					done.completeExceptionally(e);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return done;
	}


}
