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
package org.springframework.ide.eclipse.boot.dash.liveprocess;

import org.springframework.ide.eclipse.boot.dash.liveprocess.LiveDataConnectionManagementActions.ExecuteCommandAction;

/**
 * A BDE that has a correspondence to a process that can provide live data
 * (either remote or local process) should implement this interface to allow
 * the Boot Dash UI to filter elements and only show relevant live process
 * actions when the corresponding element is selected.
 * <p>
 * This interface also shows as a 'marker' to identify the types of elements
 * for which the 'Live Data Connections...' context menu should be visible.
 *
 * @author Kris De Volder
 */
public interface LiveDataCapableElement {
	boolean matchesLiveProcessCommand(ExecuteCommandAction action);
}
