/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.maven;

public class DefaultMavenConfiguration implements IMavenConfiguration {
	
	private String userSettingsFile = null;
	
	private String globalSettingsFile = null;

	public void setUserSettingsFile(String userSettingsFile) {
		this.userSettingsFile = userSettingsFile;
	}

	public void setGlobalSettingsFile(String globalSettingsFile) {
		this.globalSettingsFile = globalSettingsFile;
	}

	@Override
	public String getUserSettingsFile() {
		return userSettingsFile;
	}

	@Override
	public String getGlobalSettingsFile() {
		return globalSettingsFile;
	}

}
