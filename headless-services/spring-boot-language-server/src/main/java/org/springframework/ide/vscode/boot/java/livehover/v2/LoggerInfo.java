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

/**
 * @author Udayani V
 */
public class LoggerInfo {
	
	private String configuredLevel;
	
    private String effectiveLevel;

	public String getConfiguredLevel() {
		return configuredLevel;
	}
	public String getEffectiveLevel() {
		return effectiveLevel;
	}

}
