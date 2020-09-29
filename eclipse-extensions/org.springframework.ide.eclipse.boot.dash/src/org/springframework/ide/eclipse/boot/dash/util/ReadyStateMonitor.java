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
 * Represents an entity that actively monitors the 'ready' state of some
 * other entity (e.g. a running boot application). The ReadyStateMonitor exposes
 * a 'ready' state variable which it sets to 'true' when it observes that the
 * monitored process has booted-up succesfully.
 *
 * @author Kris De Volder
 */
public interface ReadyStateMonitor {

	/**
	 * The initial state of this will always be false. If/when the monitored enitty
	 * becomes ready this changes to true. Once this happens the ReadyStateMonitor
	 * stops watching the monitored enitity and the statevariable will no longer
	 * be updated (not even when the process terminates).
	 */
	LiveExpression<Boolean> getReady();

	/**
	 * Cleanup whatever resources are being used by the monitoring enitity.
	 * (e.g. jobs that it schedules periodically to poll and update the 'ready' state.)
	 */
	void dispose();

}
