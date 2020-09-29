/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.util;

import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * Dummy ReadyStateProvider that can be used when querying the life-cycle state
 * via JMX (or some other means) is not available for a given process.
 *
 * @author Kris De Volder
 */

public class DummyReadyStateMonitor implements ReadyStateMonitor {

	public static ReadyStateMonitor create() {
		return INSTANCE;
	}

	@Override
	public void dispose() {
	}

	@Override
	public LiveExpression<Boolean> getReady() {
		return ready;
	}

	//////////////// implementation /////////////////////////////////////

	private static final ReadyStateMonitor INSTANCE = new DummyReadyStateMonitor();
	private LiveExpression<Boolean> ready = LiveExpression.constant(true);

	private DummyReadyStateMonitor() {
		//Stateless instance, don't create, just use the singleton instance
	}
}
