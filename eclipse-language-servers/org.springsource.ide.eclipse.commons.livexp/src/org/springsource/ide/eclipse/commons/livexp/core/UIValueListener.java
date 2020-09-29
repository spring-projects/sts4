/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

/**
 * This class should be used instead of ValueListener when the code it
 * wants to execute upon receiving a 'gotValue' event is required
 * to run in the UIThread (i.e. typically this is code that needs to
 * update or read widgets in the UI).
 */
public abstract class UIValueListener<T> implements ValueListener<T> {

	private class NotifyingJob extends UIJob {

		LiveExpression<T> exp;

		public NotifyingJob(LiveExpression<T> exp) {
			super("Notifying Job");
			this.exp = exp;
			setSystem(true);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			uiGotValue(exp, exp.getValue());
			return Status.OK_STATUS;
		}

	}

	private NotifyingJob job = null;

	/**
	 * This method is final. Implement 'uiGotValue' instead.
	 */
	public final void gotValue(final LiveExpression<T> exp, final T value) {
		if (Display.getCurrent() == null) {
			NotifyingJob job = this.job;
			if (job == null || job.exp != exp) {
				job = new NotifyingJob(exp);
				this.job = job;
			}
			job.schedule();
		} else {
			uiGotValue(exp, value);
		}
	}

	/**
	 * Subclasses should implement. This method will always be called in the UIThread.
	 */
	protected abstract void uiGotValue(LiveExpression<T> exp, T value);

	/**
	 * A Lambda-friendly way to creat {@link UIValueListener}
	 */
	public static <T> UIValueListener<T> from(ValueListener<T> l) {
		return new UIValueListener<T>() {
			@Override
			protected void uiGotValue(LiveExpression<T> exp, T value) {
				l.gotValue(exp, value);
			}
		};
	}

}
