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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Udayani V
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Loggers {
	
	private List<String> levels;
    private Map<String, LoggerInfo> loggers;
    
	public List<String> getLevels() {
		return levels;
	}
	
	public Map<String, LoggerInfo> getLoggers() {
		return loggers;
	}
    
}
