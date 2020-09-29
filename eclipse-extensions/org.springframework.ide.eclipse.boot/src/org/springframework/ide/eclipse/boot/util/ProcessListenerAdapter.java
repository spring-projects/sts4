/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.util;

import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.springframework.ide.eclipse.boot.util.ProcessTracker.ProcessListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

/**
 * Adapter for {@link ProcessListener} so that subclass only needs to implement
 * the methods they are interested in.
 *
 * @author Kris De Volder
 */
public class ProcessListenerAdapter implements ProcessListener, Disposable {

	@Override
	public void debugTargetCreated(ProcessTracker tracker, IDebugTarget target) {
	}

	@Override
	public void debugTargetTerminated(ProcessTracker tracker, IDebugTarget target) {
	}

	@Override
	public void processTerminated(ProcessTracker tracker, IProcess process) {
	}

	@Override
	public void processCreated(ProcessTracker tracker, IProcess process) {
	}

	@Override
	public void dispose() {
	}

}
