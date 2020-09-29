/*******************************************************************************
 * Copyright (c) 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.api;

import java.util.EnumSet;

import org.springframework.ide.eclipse.boot.dash.model.Nameable;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;

public interface App extends Nameable {
	default EnumSet<RunState> supportedGoalStates() {
		return EnumSet.noneOf(RunState.class);
	}
	default void setGoalState(RunState inactive) {}

	RunTarget getTarget();

	default void setContext(AppContext context) {}
	default void restart(RunState runingOrDebugging) {}

	/**
	 * Override the default naming schema for the console window/view in the
	 * boot dash ui that shows output for this app. If this method returns null
	 * default naming schema is used instead.
	 */
	default String getConsoleDisplayName() { return null; }
}
