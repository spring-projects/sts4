/*******************************************************************************
 * Copyright (c) 2015, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.sections;

public class BootDashColumn {

	private final String ID;

	public BootDashColumn(String ID) {
		this.ID = ID;
	}

	@Override
	public String toString() {
		return ID;
	}

	public static final BootDashColumn RUN_STATE_ICN = new BootDashColumn("RUN_STATE_ICN");
	public static final BootDashColumn INSTANCES = new BootDashColumn("INSTANCES");
	public static final BootDashColumn PROJECT = new BootDashColumn("PROJECT");
	public static final BootDashColumn NAME = new BootDashColumn("NAME");
	public static final BootDashColumn HOST = new BootDashColumn("HOST");
	public static final BootDashColumn LIVE_PORT = new BootDashColumn("LIVE_PORT");
	public static final BootDashColumn DEFAULT_PATH = new BootDashColumn("DEFAULT_PATH");
	public static final BootDashColumn TAGS = new BootDashColumn("TAGS");
	public static final BootDashColumn EXPOSED_URL = new BootDashColumn("EXPOSED_URL");
	public static final BootDashColumn DEVTOOLS = new BootDashColumn("DEVTOOLS");
	public static final BootDashColumn TREE_VIEWER_MAIN = new BootDashColumn("TREE_VIEWER_MAIN"); //this is a 'fake' column which corresponds to the single column shown in unified tree viewer.
	public static final BootDashColumn PROGRESS = new BootDashColumn("PROGRESS");
}