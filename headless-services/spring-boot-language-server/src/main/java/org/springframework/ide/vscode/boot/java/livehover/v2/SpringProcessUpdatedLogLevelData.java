/*******************************************************************************
 * Copyright (c) 2023 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

public class SpringProcessUpdatedLogLevelData {
	
	private final ProcessType processType;
	private final String processName;
	private final String processID;
	private final String packageName;
	private final String effectiveLevel;
	private final String configuredLevel;

	public SpringProcessUpdatedLogLevelData(ProcessType processType, String processName, String processID,
			String packageName, String effectiveLevel, String configuredLevel) {
		super();
		this.processType = processType;
		this.processName = processName;
		this.processID = processID;
		this.packageName = packageName;
		this.effectiveLevel = effectiveLevel;
		this.configuredLevel = configuredLevel;

	}

	public ProcessType getProcessType() {
		return processType;
	}

	public String getProcessName() {
		return processName;
	}

	public String getProcessID() {
		return processID;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getEffectiveLevel() {
		return effectiveLevel;
	}

	public String getConfiguredLevel() {
		return configuredLevel;
	}


}
