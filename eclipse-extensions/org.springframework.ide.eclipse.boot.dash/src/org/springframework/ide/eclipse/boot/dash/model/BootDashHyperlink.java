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
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BootDashHyperlink implements ButtonModel {

	private String linkText;

	private AtomicBoolean busy = new AtomicBoolean(false);

	public BootDashHyperlink(String linkText) {
		this.linkText = linkText;
	}

	/**
	 * This overrides/implements {@link ButtonModel} with some stuff to ensure that
	 * only a single trigger of the button is active at the same time, and executes
	 * the button action in a Job.
	 * <p>
	 * Subclasses should override doPerform instead.
	 */
	final synchronized public void perform(UserInteractions ui) throws Exception {
		if (busy.compareAndSet(false, true)) {
			try {
				doPerform(ui);
			} finally {
				 busy.set(false);
			}
		}
	}

	protected abstract void doPerform(UserInteractions ui) throws Exception;

	@Override
	public String getLabel() {
		return linkText;
	}
}
