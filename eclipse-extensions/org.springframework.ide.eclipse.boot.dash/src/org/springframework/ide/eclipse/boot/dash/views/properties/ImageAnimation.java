/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.progress.UIJob;

/**
 * Images animation implementation. Changes images with a given frequency on some control
 *
 * @author Alex Boyko
 *
 */
public abstract class ImageAnimation {

	private UIJob animateJob;
	private boolean running;

	public ImageAnimation(final Image[] images, final long frequency) {
		this.animateJob = new UIJob("Image Animation Job") {

			private int counter = 0;

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (images == null || images.length == 0) {
					setFrame(null);
				} else if (images.length == 1) {
					setFrame(images[0]);
				} else {
					setFrame(images[counter % images.length]);
					counter++;
					schedule(frequency);
				}
				return Status.OK_STATUS;
			}

		};
		this.animateJob.setSystem(true);
		this.running = false;
	}

	public synchronized void start() {
		if (!running) {
			animateJob.schedule();
			running = true;
		}
	}

	public synchronized void stop() {
		if (running) {
			animateJob.cancel();
			running = false;
		}
	}

	abstract protected void setFrame(Image image);

}
