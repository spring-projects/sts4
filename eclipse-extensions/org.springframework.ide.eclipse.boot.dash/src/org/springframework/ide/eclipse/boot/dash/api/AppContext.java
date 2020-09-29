/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.api;

import org.springframework.ide.eclipse.boot.dash.model.remote.RefreshStateTracker;

/**
 * AppContext instance is provided to an App by the boot dash via {@link App}.setContext
 * method. The app context provides extension point authors easy access to
 * the 'context' in which this app is shown in boot dash ui.
 */
public interface AppContext {

	/**
	 * RefreshStateTracker provides extension point authors a way to execute
	 * long running operations associated with an app. While the work is
	 * executing an indication of this, including a 'busy' message will be shown
	 * somewhere in the boot dash ui.
	 */
	RefreshStateTracker getRefreshTracker();

	boolean projectHasDevtoolsDependency();

}
