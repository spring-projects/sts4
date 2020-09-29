/*******************************************************************************
 * Copyright (c) 2012, 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.util;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.springsource.ide.eclipse.commons.frameworks.core.FrameworkCoreActivator;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class JobUtil {
	
	public interface JobBody {
		void run(IProgressMonitor mon) throws Exception;
	}

	public static <T> T interruptAfter(Duration waitFor, Callable<T> body) throws Exception {
		Thread self = Thread.currentThread();
		Job job = new Job("Interupter") {
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				self.interrupt();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule(waitFor.toMillis());
		try {
			return body.call();
		} catch (InterruptedException e) {
			Log.log(e);
			throw new TimeoutException();
		} finally {
			job.cancel();
		}
	}
	
	/**
	 * Should ideally not be used except for testing. Eclipse UI should provide a runnable context like
	 * a progress service via the workbench.
	 */
	public static final IRunnableContext DEFAULT_BACKGROUND_RUNNABLE_CONTEXT = new IRunnableContext() {

		@Override
		public void run(boolean arg0, boolean arg1, final IRunnableWithProgress runnableWithProgress)
				throws InvocationTargetException, InterruptedException {
			if (runnableWithProgress != null) {
				Job job = new Job("Running a background job.") {

					@Override
					public IStatus run(IProgressMonitor monitor) {
						try {
							runnableWithProgress.run(monitor);
						} catch (InvocationTargetException | InterruptedException e) {
							return UIJob.errorStatus(e);
						}
						return Status.OK_STATUS;
					}
				};
				job.setSystem(true);
				job.schedule();

			}
		}
	};

	/**
	 * Create a scheduling rule that conflicts only with itself and only
	 * contains itself. Jobs that want to have a 'light' impact on blocking
	 * other jobs but still some guarantee that they won't trample over other
	 * things that require access to some internal shared resource that only
	 * they can access should use this rule to protect the resource.
	 */
	public static ISchedulingRule lightRule(final String name) {
		return new ISchedulingRule() {
			public boolean contains(ISchedulingRule rule) {
				return rule == this;
			}

			public boolean isConflicting(ISchedulingRule rule) {
				return rule == this || rule.contains(this);
			}

			public String toString() {
				return name;
			};
		};
	}

	/**
	 * Runs a job in the background through a progress service that provides UI
	 * progress. Because the progress service provides UI progress, the initial
	 * launching of the services is done from the UI thread BEFORE the
	 * background job is started . A progress service is required and cannot be
	 * null.
	 * 
	 */
	public static void runBackgroundJobWithUIProgress(final IRunnableWithProgress runnableWithProgress,
			final IRunnableContext progressService, final String jobLabel) throws Exception {
		// This outer runnable launches the background job
		final IRunnableWithProgress outerRunnable = new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(jobLabel, IProgressMonitor.UNKNOWN);
				// Fork outside UI thread
				Job job = new Job(jobLabel) {
					@Override
					public IStatus run(IProgressMonitor monitor) {
						SubMonitor subMonitor = SubMonitor.convert(monitor);
						subMonitor.setTaskName(jobLabel);
						try {
							runnableWithProgress.run(subMonitor);
						} catch (Throwable e) {
							FrameworkCoreActivator.log(e);
						} finally {
							subMonitor.done();
						}
						return Status.OK_STATUS;
					}

				};
				job.schedule();
			}
		};

		// Progress services needs to be launched in UI thread.
		Exception[] error = new Exception[1];
		Display.getDefault().syncExec(() -> {
			try {
				progressService.run(true, true, outerRunnable);
			} catch (InvocationTargetException e) {
				error[0] = e;
			} catch (InterruptedException e) {
				error[0] = e;
			}
		});

		if (error[0] != null) {
			throw error[0];
		}
	}

	public static CompletableFuture<Void> runQuietlyInJob(String jobName, JobBody doit) {
		CompletableFuture<Void> f = new CompletableFuture<>();
		Job job = new Job(jobName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					doit.run(monitor);
					f.complete(null);
				} catch (Throwable e) {
					f.completeExceptionally(e);
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
			
		};
		job.setSystem(true);
		job.schedule();
		return f;
	}

	public static CompletableFuture<Void> runInJob(String jobName, JobBody doit) {
		CompletableFuture<Void> f = new CompletableFuture<>();
		Job job = new Job(jobName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					doit.run(monitor);
					f.complete(null);
				} catch (Throwable e) {
					f.completeExceptionally(e);
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
		return f;
	}

}
